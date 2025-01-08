#!/usr/bin/env zsh

# Setup TestContainers with Colima and docker
# ===========================================
# Simplify setting up TestContainers to use Colima with Docker by configuring the appropriate environment variables
# as documented: https://java.testcontainers.org/supported_docker_environment/#colima
#
# Usage
# =====
# Start Colima with `colima start --network-address`. Then choose one of the follow options depending on if the
# current shell needs to be modified.
#
# - `source ./setup-tc-colima.sh` to enable current shell to have TC use Colima.
#   This will also print a semi-colon-delimited list of environment variables.
# - `./setup-tc-colima.sh` to ONLY print a semi-colon-delimited list of environment variables. The current shell will be unchanged.
#   These environment variables can be directly pasted into IntelliJ's run configuration's environment variables: https://www.baeldung.com/intellij-idea-environment-variables
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

export_vars() {
	read -r -A env_vars <<<"$@"
	for i in "${env_vars[@]}"; do
		export "${i?}"
	done
}

print_delimited() {
	read -r -A env_vars <<<"$@"
	local IFS=";"
	print -n "${env_vars[*]}" # print without newline at the end; otherwise, cannot directly copy/paste into IntelliJ due to parsing error
}

# Only print the environment variables. Do not modify the current shell.
print_vars() {
	read -r -A env_vars <<<"$(define_vars)"

	print_delimited "${env_vars[@]}"
}

# Modify the current shell to use TC with Colima. Also print the environment variables
setup_colima() {
	read -r -A env_vars <<<"$(define_vars)"

	export_vars "${env_vars[@]}"

	print_delimited "${env_vars[@]}"
}

if [[ $ZSH_EVAL_CONTEXT == 'toplevel' ]]; then
	print_vars
else
	setup_colima
fi
