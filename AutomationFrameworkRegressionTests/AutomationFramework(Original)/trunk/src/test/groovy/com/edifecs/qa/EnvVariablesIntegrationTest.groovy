package com.edifecs.qa

import org.testng.Assert;
import org.testng.annotations.Test;


class EnvVariablesIntegrationTest {
    def ECRootPath
    @Test
    void testECRootPath(){
        ECRootPath=System.getenv('ECRootPath')
        Assert.assertNotNull('ECRootPath environment variable expected not to be null', ECRootPath )
    }

    @Test
    void testXERoot(){
        def XERoot=System.getenv('XERoot')
        Assert.assertNotNull('XERoot environment variable expected not to be null', XERoot )
    }

    @Test
    void testXESRoot(){
        def XESRoot=System.getenv('XESRoot')
        Assert.assertNotNull('XESRoot environment variable expected not to be null', XESRoot )
    }

}

