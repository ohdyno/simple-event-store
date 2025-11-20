#!/usr/bin/env fish

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
# - fish
#
# Tested On
# =========
# - colima version 0.8.1
# - fish shell
# - macOS 15.2 (24C101) x86_64
# - IntelliJ IDEA Ultimate 2024.3.1.1

# Define environment variables using values extracted from Colima
function define_vars
    set colima_status (colima status --json)

    set address (echo $colima_status | jq --raw-output '.ip_address')

    if test -z "$address"
        echo "Missing Address. Make sure colima is started with --network-address"
        return 1
    end

    set docker_host (echo $colima_status | jq --raw-output '.docker_socket')

    set docker_socket "/var/run/docker.sock"

    echo "TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=$docker_socket TESTCONTAINERS_HOST_OVERRIDE=$address DOCKER_HOST=$docker_host"
end

function print_delimited
    # Read STDIN and split into array
    read --local input
    set env_vars (string split ' ' $input)
    # Join with semicolon delimiter and print without newline
    printf '%s' (string join ';' $env_vars)
end

function print_for_jetbrains
    set vars (define_vars)
    if test $status -eq 0
        echo $vars | print_delimited
    else
        echo $vars
    end
end

function print_for_export
    echo export (define_vars)
end

function print_help
    set script_name $argv[1]
    echo "$script_name \
produces environment variables that enables TestContainers to use Colima with Docker.

Note: Colima must be started with --network address e.g. colima start --network-address.

Usage:
$script_name [--jb | --sh]

Flags:
 --start 	 Re/Start Colima correctly
 
 --jb 		 Print semi-colon delimited environment variables that can be copy/pasted into IntelliJ
			 Example: $script_name --jb | pbcopy
			 
 --sh 		 Print an export command for the environment variables that can be 'eval()' to change current shell
			 Example: eval \$($script_name --sh)
			 
 -h 		 Print this message
"
end

function start_colima
    colima stop
    colima start --network-address
end

function run_script
    set script_name $argv[1]

    # Initialize flags
    set show_help true
    set use_jetbrains false
    set use_shell false
    set do_start false

    # Parse flags
    for flag in $argv
        switch $flag
            case --jb
                set use_jetbrains true
                set show_help false
            case --sh
                set use_shell true
                set show_help false
            case --start
                set do_start true
                set show_help false
        end
    end

    if test $show_help = true
        print_help $script_name
    end

    if test $do_start = true
        # Redirect to current terminal without polluting STDOUT
        start_colima >(tty)
    end

    if test $use_jetbrains = true
        print_for_jetbrains
    end

    if test $use_shell = true
        print_for_export
    end
end

# Run the script with all arguments
run_script (status filename) $argv
