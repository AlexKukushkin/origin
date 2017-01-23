Steps to reproduce the scenario of the bug:
==========================================
1. Process Claim and Payment. Get compliance exception.
2. Process cleanup. Claim (EUO_Cleanup = true).
   Process Payment for cleanup (EUO_Cleanup = true), Get exception.
3. Add AdmissionDate at SV level. 
4. Perform revalidation.

PS: In Payment.xml ServiceAdjustment is missing.