package edu.iis.mto.serverloadbalancer;

import static edu.iis.mto.serverloadbalancer.CurrentLoadPercentageMatcher.hasLoadPercentageOf;
import static edu.iis.mto.serverloadbalancer.ServerBuilder.server;
import static edu.iis.mto.serverloadbalancer.VmBuilder.vm;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(value = Parameterized.class)
public class ServerLoadBalancerParametrizedTest extends ServerLoadBalancerBaseTest {

    @Parameterized.Parameters public static Collection<Object[]> defaultDataToTest() {
        return Arrays.asList(
                new Object[][] {{0, 0, 0d, false}, {0, 1, 0d, false}, {1, 0, 0d, true}, {1, 1, 100.0d, true}, {1, 2, 0d, false}, {2, 1, 50.0d, true}, {2, 2, 100.0d, true}});
    }

    @Parameterized.Parameter(value = 0) public int inputServerCapacity;

    @Parameterized.Parameter(value = 1) public int inputVmSize;

    @Parameterized.Parameter(value = 2) public double expectedLoadPercentage;

    @Parameterized.Parameter(value = 3) public boolean expectedServerContainsVm;

    @Test public void balancingOneServerWithOneSlotCapacity_andOneSlotVm_fillsTheServerWithTheVm() {
        Server theServer = a(server().withCapacity(inputServerCapacity));
        Vm theVm = a(vm().ofSize(inputVmSize));
        balance(aListOfServersWith(theServer), aListOfVmsWith(theVm));

        assertThat(theServer, hasLoadPercentageOf(expectedLoadPercentage));
        assertThat(theServer.contains(theVm), is(expectedServerContainsVm));
    }



}
