package cicd.security

import rego.v1

default result := {
    "decision": "ALLOW",
    "message": "Pipeline satisfies security policy."
}

result := {
    "decision": "BLOCK",
    "message": "Critical findings exceed policy threshold."
} if input.critical > 0

else := {
    "decision": "BLOCK",
    "message": "High findings exceed allowed threshold (10)."
} if input.high > 10

else := {
    "decision": "WARNING",
    "message": "High findings detected."
} if input.high > 0

else := {
    "decision": "WARNING",
    "message": "Medium findings exceed warning threshold (20)."
} if input.medium > 20

else := {
    "decision": "WARNING",
    "message": "Large number of low severity findings detected."
} if input.low > 50

else := {
    "decision": "WARNING",
    "message": "Informational findings exceed 100."
} if input.info > 100