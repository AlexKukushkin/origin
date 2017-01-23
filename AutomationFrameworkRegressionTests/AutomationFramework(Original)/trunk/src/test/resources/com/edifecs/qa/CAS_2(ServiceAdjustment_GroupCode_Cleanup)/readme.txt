Steps to reproduce the scenario of the bug:
==========================================
1. Process Claim and Payment. Get compliance exception.
2. Process cleanup. Claim (EUO_Cleanup = true).
   Process Payment for cleanup (EUO_Cleanup = true), Get exception.
3. Process Claim and Payment.(EUO_Cleanup = false). Merging process. Get exception.
4. Process Claim and Payment. (EUO_Cleanup = true). Replacement process. Get exception.
5. Add AdmissionDate at SV level. 
6. Perform revalidation.

PS: In Payment.xml ServiceAdjustment GroupCode is missing.