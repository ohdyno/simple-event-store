# source the file with source ./setup-tc-colima.sh
# tested using ZSH
# requires the program jq (https://jqlang.github.io/jq/)
# it will export the appropriate variables to support TestContainers using Colima: https://java.testcontainers.org/supported_docker_environment/#colima
# and it will print a comma-delimited variables that can be copied to IntelliJ's run configuration's environment variables: https://www.baeldung.com/intellij-idea-environment-variables

setup_colima() {
    define_vars | read -r -A env_vars

    export_vars "${env_vars[@]}"

    print_comma_delimited "${env_vars[@]}"
}

define_vars() {
    local colima_status
    colima_status=$(colima status -j)

    local address
    address=$(jq -r '.ip_address' <<< $colima_status)

    if [[ -z "${address}" ]]; then
        print "Missing Address. Make sure colima is started with --network-address"
        exit 1
    fi

    local docker_host
    docker_host=$(jq -r '.docker_socket' <<< $colima_status)

    local docker_socket="/var/run/docker.sock"

    print "TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=$docker_socket TESTCONTAINERS_HOST_OVERRIDE=$address DOCKER_HOST=$docker_host"
}

export_vars() {
    read -r -A env_vars <<< "$@"
    for i in "${env_vars[@]}"
    do
        export "${i?}"
    done
}

print_comma_delimited() {
    read -r -A env_vars <<< "$@"
    local IFS=","
    print "${env_vars[*]}"
}

setup_colima
