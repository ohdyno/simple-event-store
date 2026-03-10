#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd -- "${SCRIPT_DIR}/../.." && pwd)"
DIAGRAMS_DIR="${SCRIPT_DIR}"
OUTPUT_DIR="${REPO_ROOT}/target/mermaid-ascii"

ASCII_MODE=false
COORDS_MODE=false
STDOUT_MODE=false
LIST_MODE=false
LIST_NAMES_MODE=false
VERBOSE_MODE=false

PADDING_X=""
PADDING_Y=""
BORDER_PADDING=""

declare -a REQUESTED_DIAGRAMS=()
declare -a MERMAID_COMMAND=()

require_mise() {
	if ! command -v mise >/dev/null 2>&1; then
		printf 'Missing dependency: mise is required to run %s\n' "$0" >&2
		printf 'Install mise and ensure it is available on PATH.\n' >&2
		return 1
	fi
}

print_help() {
	cat <<EOF
Render Mermaid source files from ${DIAGRAMS_DIR}.

This script uses mise to provide the mermaid-ascii tool.

Usage:
  $(basename "$0") [flags]

Defaults:
  - Renders every .mmd file in the same directory as this script
  - Writes rendered output to ${OUTPUT_DIR}

Flags:
  --all                    Render all diagrams (default when no diagram is selected)
  -d, --diagram VALUE      Render one diagram by stem, filename, or path
  -l, --list               List available diagrams and exit
  -o, --output-dir DIR     Output directory for rendered text files
  -s, --stdout             Print rendered output to stdout instead of writing files
  -a, --ascii              Use ASCII only
  -c, --coords             Show coordinates
  -x, --padding-x INT      Horizontal space between nodes
  -y, --padding-y INT      Vertical space between nodes
  -p, --border-padding INT Padding between text and border
  -v, --verbose            Enable mermaid-ascii verbose mode
  -h, --help               Print this message

Examples:
  $(basename "$0")
  $(basename "$0") --list
  $(basename "$0") --diagram save-flow
  $(basename "$0") --diagram 2 --stdout
  $(basename "$0") --ascii --output-dir target/mermaid-ascii/plain
EOF
}

raw_diagram_paths() {
	find "${DIAGRAMS_DIR}" -maxdepth 1 -type f -name '*.mmd' -print | sort
}

list_diagram_names() {
	local candidate=""
	local basename=""
	while IFS= read -r candidate; do
		basename="$(basename -- "${candidate}")"
		printf '%s\n' "${basename%.mmd}"
	done < <(raw_diagram_paths)
}

list_diagrams() {
	local index=1
	local candidate=""
	local basename=""
	printf 'Available diagrams:\n'
	while IFS= read -r candidate; do
		basename="$(basename -- "${candidate}")"
		printf '  %d. %s (%s)\n' "${index}" "${basename%.mmd}" "${basename}"
		index=$((index + 1))
	done < <(raw_diagram_paths)
}

resolve_by_index() {
	local requested_index="$1"
	local index=1
	local candidate=""
	while IFS= read -r candidate; do
		if [[ "${index}" -eq "${requested_index}" ]]; then
			printf '%s\n' "${candidate}"
			return 0
		fi
		index=$((index + 1))
	done < <(raw_diagram_paths)

	printf 'Unknown diagram index: %s\n' "${requested_index}" >&2
	return 1
}

resolve_by_name() {
	local value="$1"
	local exact_match=""
	local prefix_match=""
	local candidate=""
	local basename=""
	local stem=""

	while IFS= read -r candidate; do
		basename="$(basename -- "${candidate}")"
		stem="${basename%.mmd}"

		if [[ "${basename}" == "${value}" || "${stem}" == "${value}" ]]; then
			if [[ -n "${exact_match}" ]]; then
				printf 'Ambiguous diagram selector: %s\n' "${value}" >&2
				return 1
			fi
			exact_match="${candidate}"
		fi
	done < <(raw_diagram_paths)

	if [[ -n "${exact_match}" ]]; then
		printf '%s\n' "${exact_match}"
		return 0
	fi

	while IFS= read -r candidate; do
		basename="$(basename -- "${candidate}")"
		stem="${basename%.mmd}"

		if [[ "${basename}" == "${value}"* || "${stem}" == "${value}"* ]]; then
			if [[ -n "${prefix_match}" ]]; then
				printf 'Ambiguous diagram selector: %s\n' "${value}" >&2
				printf 'Use one of:\n' >&2
				printf '  - %s\n' "$(basename -- "${prefix_match}" .mmd)" >&2
				printf '  - %s\n' "${stem}" >&2
				return 1
			fi
			prefix_match="${candidate}"
		fi
	done < <(raw_diagram_paths)

	if [[ -n "${prefix_match}" ]]; then
		printf '%s\n' "${prefix_match}"
		return 0
	fi

	printf 'Unknown diagram selector: %s\n' "${value}" >&2
	printf 'Run %s --list to see available diagrams.\n' "$(basename "$0")" >&2
	return 1
}

resolve_diagram() {
	local value="$1"
	local absolute_dir=""

	if [[ -f "${value}" ]]; then
		absolute_dir="$(cd -- "$(dirname -- "${value}")" && pwd)"
		printf '%s/%s\n' "${absolute_dir}" "$(basename -- "${value}")"
		return 0
	fi

	if [[ -f "${REPO_ROOT}/${value}" ]]; then
		printf '%s\n' "${REPO_ROOT}/${value}"
		return 0
	fi

	if [[ -f "${DIAGRAMS_DIR}/${value}" ]]; then
		printf '%s\n' "${DIAGRAMS_DIR}/${value}"
		return 0
	fi

	if [[ -f "${DIAGRAMS_DIR}/${value}.mmd" ]]; then
		printf '%s\n' "${DIAGRAMS_DIR}/${value}.mmd"
		return 0
	fi

	if [[ "${value}" =~ ^[0-9]+$ ]]; then
		resolve_by_index "${value}"
		return 0
	fi

	resolve_by_name "${value}"
}

build_mermaid_command() {
	local source_file="$1"

	MERMAID_COMMAND=(mise exec mermaid-ascii -- mermaid-ascii -f "${source_file}")

	if [[ "${ASCII_MODE}" == true ]]; then
		MERMAID_COMMAND+=(-a)
	fi

	if [[ "${COORDS_MODE}" == true ]]; then
		MERMAID_COMMAND+=(-c)
	fi

	if [[ -n "${PADDING_X}" ]]; then
		MERMAID_COMMAND+=(-x "${PADDING_X}")
	fi

	if [[ -n "${PADDING_Y}" ]]; then
		MERMAID_COMMAND+=(-y "${PADDING_Y}")
	fi

	if [[ -n "${BORDER_PADDING}" ]]; then
		MERMAID_COMMAND+=(-p "${BORDER_PADDING}")
	fi

	if [[ "${VERBOSE_MODE}" == true ]]; then
		MERMAID_COMMAND+=(-v)
	fi
}

render_to_stdout() {
	local source_file="$1"

	build_mermaid_command "${source_file}"
	"${MERMAID_COMMAND[@]}"
}

render_to_file() {
	local source_file="$1"
	local filename
	filename="$(basename -- "${source_file}" .mmd)"
	local destination="${OUTPUT_DIR}/${filename}.txt"

	mkdir -p "${OUTPUT_DIR}"
	build_mermaid_command "${source_file}"
	if ! "${MERMAID_COMMAND[@]}" >"${destination}"; then
		rm -f "${destination}"
		printf 'Failed to render %s\n' "${source_file}" >&2
		return 1
	fi
	printf 'Rendered %s -> %s\n' "${source_file}" "${destination}"
}

main() {
	while [[ $# -gt 0 ]]; do
		case "$1" in
		--all)
			shift
			;;
		-d | --diagram)
			if [[ $# -lt 2 ]]; then
				printf 'Missing value for %s\n' "$1" >&2
				return 1
			fi
			REQUESTED_DIAGRAMS+=("$2")
			shift 2
			;;
		-l | --list)
			LIST_MODE=true
			shift
			;;
		--list-names)
			LIST_NAMES_MODE=true
			shift
			;;
		-o | --output-dir)
			if [[ $# -lt 2 ]]; then
				printf 'Missing value for %s\n' "$1" >&2
				return 1
			fi
			OUTPUT_DIR="$2"
			shift 2
			;;
		-s | --stdout)
			STDOUT_MODE=true
			shift
			;;
		-a | --ascii)
			ASCII_MODE=true
			shift
			;;
		-c | --coords)
			COORDS_MODE=true
			shift
			;;
		-x | --padding-x)
			if [[ $# -lt 2 ]]; then
				printf 'Missing value for %s\n' "$1" >&2
				return 1
			fi
			PADDING_X="$2"
			shift 2
			;;
		-y | --padding-y)
			if [[ $# -lt 2 ]]; then
				printf 'Missing value for %s\n' "$1" >&2
				return 1
			fi
			PADDING_Y="$2"
			shift 2
			;;
		-p | --border-padding)
			if [[ $# -lt 2 ]]; then
				printf 'Missing value for %s\n' "$1" >&2
				return 1
			fi
			BORDER_PADDING="$2"
			shift 2
			;;
		-v | --verbose)
			VERBOSE_MODE=true
			shift
			;;
		-h | --help)
			print_help
			return 0
			;;
		*)
			printf 'Unknown argument: %s\n\n' "$1" >&2
			print_help >&2
			return 1
			;;
		esac
	done

	if [[ "${LIST_MODE}" == true ]]; then
		list_diagrams
		return 0
	fi

	if [[ "${LIST_NAMES_MODE}" == true ]]; then
		list_diagram_names
		return 0
	fi

	require_mise

	if [[ "${STDOUT_MODE}" == false && "${OUTPUT_DIR}" != /* ]]; then
		OUTPUT_DIR="${REPO_ROOT}/${OUTPUT_DIR}"
	fi

	local -a diagrams=()
	if [[ ${#REQUESTED_DIAGRAMS[@]} -eq 0 ]]; then
		while IFS= read -r candidate; do
			diagrams+=("${candidate}")
		done < <(raw_diagram_paths)
	else
		local requested=""
		for requested in "${REQUESTED_DIAGRAMS[@]}"; do
			diagrams+=("$(resolve_diagram "${requested}")")
		done
	fi

	if [[ ${#diagrams[@]} -eq 0 ]]; then
		printf 'No Mermaid diagrams found in %s\n' "${DIAGRAMS_DIR}" >&2
		return 1
	fi

	if [[ "${STDOUT_MODE}" == true && ${#diagrams[@]} -ne 1 ]]; then
		printf '--stdout requires exactly one diagram. Use --diagram to select one.\n' >&2
		return 1
	fi

	local diagram=""
	for diagram in "${diagrams[@]}"; do
		if [[ "${STDOUT_MODE}" == true ]]; then
			render_to_stdout "${diagram}"
		else
			render_to_file "${diagram}"
		fi
	done
}

main "$@"
