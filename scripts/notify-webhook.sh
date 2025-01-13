#!/bin/bash

# Variables
WEBHOOK_URL="http://localhost:7010/api/webhook/download-complete"
TORRENT_ID="$TR_TORRENT_ID"
TORRENT_NAME="$TR_TORRENT_NAME"
TORRENT_DIR="$TR_TORRENT_DIR"

# Payload
PAYLOAD=$(cat <<EOF
{
  "id": "$TORRENT_ID",
  "name": "$TORRENT_NAME",
  "downloadDir": "$TORRENT_DIR"
}
EOF
)

# Enviar notificación
curl -X POST -H "Content-Type: application/json" -d "$PAYLOAD" "$WEBHOOK_URL"
