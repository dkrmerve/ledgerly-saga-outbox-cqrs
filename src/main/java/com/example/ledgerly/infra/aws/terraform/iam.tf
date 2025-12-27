# IAM Roles (Design Only)

# Principle: least privilege

# API task role:
# - Read secrets (JWT, Masterpass token)
# - Write logs/metrics

# Worker task role:
# - Same as API
# - Kafka access
# - Redis access
