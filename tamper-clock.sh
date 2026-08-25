#!/usr/bin/env bash

set -euo pipefail

show_time() {
    echo "System time: $(date --iso-8601=seconds)"
}

case "${1:-}" in
rollback)
    echo "Current time:"
    show_time

    sudo timedatectl set-ntp false
    sudo date --set="1 hour ago"

    echo "Current time:"
    show_time
    ;;

restore)
    sudo timedatectl set-ntp true

    if systemctl is-active --quiet chronyd.service; then
        sudo chronyc makestep
    elif systemctl is-active --quiet systemd-timesyncd.service; then
        sudo systemctl restart systemd-timesyncd.service
    fi

    echo "Current time:"
    show_time
    ;;

status)
    show_time
    timedatectl status
    ;;

*)
    echo "  $0 rollback"
    echo "  $0 restore"
    echo "  $0 status"
    exit 1
    ;;
esac
