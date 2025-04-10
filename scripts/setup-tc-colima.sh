#!/usr/bin/env zsh

# Setup TestContainers with Colima and docker
# ===========================================
# Simplify setting up TestContainers to use Colima with Docker by configuring the appropriate environment variables
# as documented: https://java.testcontainers.org/supported_docker_environment/#colima
#
# Usage
# =====
# Run this script without any arguments to see intended usage, or checkout content of print_help()
#
# Requirements
# ============
# - colima started with `--network-address`: `colima start --network-address`
# - jq for parsing JSON (https://jqlang.github.io/jq/)
# - zsh
#
# Tested On
# =========
# - colima version 0.8.1
# - zsh 5.9
# - macOS 15.2 (24C101) x86_64
# - IntelliJ IDEA Ultimate 2024.3.1.1

# Define environment variables using values extracted from Colima
define_vars() {
	local colima_status
	colima_status=$(colima status -j)

	local address
	address=$(jq -r '.ip_address' <<<"$colima_status")

	if [[ -z "${address}" ]]; then
		print "Missing Address. Make sure colima is started with --network-address"
		exit 1
	fi

	local docker_host
	docker_host=$(jq -r '.docker_socket' <<<"$colima_status")

	local docker_socket="/var/run/docker.sock"

	print "TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=$docker_socket TESTCONTAINERS_HOST_OVERRIDE=$address DOCKER_HOST=$docker_host"
}

print_delimited() {
	read -r -A env_vars
	local IFS=";"
	print -n "${env_vars[*]}" # print without newline at the end; otherwise, cannot directly copy/paste into IntelliJ due to parsing error
}

print_for_jetbrains() {
	local vars
	if vars=$(define_vars); then
		print "$vars" | print_delimited
	else
		print "$vars"
	fi
}

print_for_export() {
	print "export" "$(define_vars)"
}

print_help() {
	local script_name="$1"
	print "$script_name" "produces environment variables that enables TestContainers to use Colima with Docker."
	print ""
	print "Note:" "Colima must be started with --network address e.g. colima start --network-address."
	print ""
	print "Usage:"
	print "$script_name" "[--jb | --sh]"
	print ""
	print "Flags:"
	print "\t" "--start" "\t" "Re/Start Colima correctly"
	print ""
	print "\t" "--jb" "\t\t" "Print semi-colon delimited environment variables that can be copy/pasted into IntelliJ"
	print "\t\t\t" "Example: $script_name --jb | pbcopy"
	print ""
	print "\t" "--js" "\t\t" "Print an export command for the environment variables that can be 'eval()' to change current shell"
	print "\t\t\t" "Example: eval \$($script_name --sh)"
	print ""
	print "\t" "-h" "\t\t" "Print this message"
}

start_colima() {
	colima stop
	colima start --network-address
}

run_script() {
	local script_name="$1"

	# Parse Flags
	declare -A flags
	flags=([help]=true)
	for flag in "${@:1}"; do
		if [[ "$flag" == "--jb" ]]; then
			flags[jetbrains]=true
			flags[help]=false
		elif [[ "$flag" == "--sh" ]]; then
			flags[shell]=true
			flags[help]=false
		elif [[ "$flag" == "--start" ]]; then
			flags[start]=true
			flags[help]=false
		fi
	done

	if [[ true == "${flags[help]}" ]]; then
		print_help "$script_name"
	fi

	if [[ true == "${flags[start]}" ]]; then
		start_colima >"$(tty)"
	fi

	if [[ true == "${flags[jetbrains]}" ]]; then
		print_for_jetbrains
	fi

	if [[ true == "${flags[shell]}" ]]; then
		print_for_export
	fi
}

run_script "$0" "$@"
