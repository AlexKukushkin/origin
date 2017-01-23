Steps to reproduce the scenario of the bug:
==========================================
1. Process Original Claim and Payment. (The Subscriber Info node is missing).
2. Get exception.
3. Process Claim and Payment for Cleanup.
4. Check if there are no exceptions in the routes for Internal Profile. The outbound encounter should be successfully generated.