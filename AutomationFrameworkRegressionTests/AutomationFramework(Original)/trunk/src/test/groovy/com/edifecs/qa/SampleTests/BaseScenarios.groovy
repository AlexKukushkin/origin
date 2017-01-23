package com.edifecs.qa.SampleTests

import com.edifecs.qa.utils.CopyFiles
import com.edifecs.qa.utils.SimpleTests

//import com.edifecs.qa.utils.UtilsQuery
import org.testng.annotations.Test

/**
 * Created with IntelliJ IDEA.
 * User: InaG
 * Date: 7/1/13
 * Time: 12:00 PM
 * To change this template use File | Settings | File Templates.
 */
class BaseScenarios extends SimpleTests{

    @Test
    void RegularEncounter(){
        inboundClaim("New", "RegularEncounter/ECHCF_TC1_NewSub.xml", "RegularEncounter/ECHCF_TC1_NewSub.xml.properties")
        claimUpdates("Update", "RegularEncounter/ECHCF_TC1_EncRep.xml", "/RegularEncounter/ECHCF_TC1_EncRep.xml.properties")
        claimTrigger("Trigger", "RegularEncounter/ECHCF_TC1_EncRep2.xml", "/RegularEncounter/ECHCF_TC1_EncRep2.xml.properties")
        claimAcks("TA1", "RegularEncounter/TA1.dat")
        claimAcks("999", "RegularEncounter/999.dat")
        claimAcks("277","RegularEncounter/277CA.dat")
    }

}
