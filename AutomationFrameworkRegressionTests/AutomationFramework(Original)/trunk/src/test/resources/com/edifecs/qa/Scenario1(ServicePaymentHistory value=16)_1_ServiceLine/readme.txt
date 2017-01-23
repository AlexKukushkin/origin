Steps to reproduce the scenario of the bug:
==========================================
1. Process Claim.xml + Claim.xml.properties.
2. Process Payment.xml + Payment.xml.properties(With one ServiceLine and one ServicePaymentHistory value = 16).
3. Process Payment.xml + Payment.xml.properties from folder Payment2(With one ServiceLine and one ServicePaymentHistory value = MC).
4. One line payment is dropped after merging process. Check in tracer.