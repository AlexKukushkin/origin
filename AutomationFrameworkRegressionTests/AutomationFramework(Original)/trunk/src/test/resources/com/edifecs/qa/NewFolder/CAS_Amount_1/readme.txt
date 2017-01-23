Steps to reproduce the scenario of the bug:
==========================================
1. Process Claim and Payment. Get compliance exception.
2. Process cleanup. Claim (EUO_Cleanup = true).
   Process Payment for cleanup (EUO_Cleanup = true).

3. Check if there is no extra CAS segment in the generated outbound encounter.