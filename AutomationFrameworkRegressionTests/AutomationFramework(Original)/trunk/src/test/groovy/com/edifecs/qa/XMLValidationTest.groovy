package com.edifecs.qa

import org.testng.Assert;
import org.testng.annotations.Test;
import com.edifecs.qa.utils.Utils;
import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory

class XMLValidationTest extends Utils {
    def factory = null;

    void validate(String schemaPath, String xmlPath) throws Exception {
        def schema = factory.newSchema(new StreamSource(new File(schemaPath)));
        def validator = schema.newValidator();
        validator.validate(new StreamSource(new File(xmlPath)));
    }

    @Test(dependsOnMethods="checkIfFileConnectorsExist")
    void xmlValidationsSetup() {
        factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
    }

    @Test(dependsOnMethods="xmlValidationsSetup")
    void htrMetadata() {
        def schemaPath = ECRootPath + "/TM/ServiceManager/classes/HTRMetadataConfig.xsd";
        validate(schemaPath, ECRootPath + "/EUO/CLM/setup/metadata/EUO_htr_metadata.xml");
    }

    @Test(dependsOnMethods="xmlValidationsSetup")
    void agreements() {
        def schemaPath = ECRootPath + "/TM/ServiceManager/tools/agreements-import-export/resources/AgreementsImportExport.xsd";
        validate(schemaPath, ECRootPath + "/EUO/CLM/setup/metadata/EUO_Agreements.xml");
    }

    @Test(dependsOnMethods="xmlValidationsSetup")
    void tpmMetadata() {
        def schemaPath = ECRootPath + "/TM/ServiceManager/tools/tpm-metadata-update/resources/TpmMetadataImportExport.xsd";
        validate(schemaPath, ECRootPath + "/EUO/CLM/setup/metadata/EUO_tpm_metadata.xml");
    }

    @Test(dependsOnMethods="xmlValidationsSetup")
    void partners() {
        def schemaPath = ECRootPath + "/TM/ServiceManager/tools/partners-import-export/resources/PartnersImportExport.xsd";
        validate(schemaPath, ECRootPath + "/EUO/CLM/RIM/community/EUO_partners.xml");
    }

    @Test(dependsOnMethods="xmlValidationsSetup")
    void dataSplitRules() {
        def schemaPath = ECRootPath + "/XEngine/config/ldns/DataSplitRules.xsd";
        validate(schemaPath, ECRootPath + "/EUO/CLM/config/splitRules/EUO_SplitByClaim.xml");
        validate(schemaPath, ECRootPath + "/EUO/CLM/config/splitRules/EUO_SplitClaim.xml");
        validate(schemaPath, ECRootPath + "/EUO/CLM/config/splitRules/EUO_SplitTransaction.xml");
    }
}
