Steps to reproduce the scenario of the bug:
==========================================
1. Process Original Claim and Payment. (The DTP Admission Date is missing in Claim).
2. Get compliance rejection exception.
3. Process Claim and Payment for Cleanup.
4. Check if there are no exceptions in the routes for Internal Profile. The outbound encounter should be successfully generated.