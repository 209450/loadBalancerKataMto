package edu.iis.mto.serverloadbalancer;

import static edu.iis.mto.serverloadbalancer.CurrentLoadPercentageMatcher.hasLoadPercentageOf;
import static edu.iis.mto.serverloadbalancer.ServerBuilder.server;
import static edu.iis.mto.serverloadbalancer.ServerVmsCountMatcher.hasVmsCountOf;
import static edu.iis.mto.serverloadbalancer.VmBuilder.vm;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.After;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;

import java.util.Arrays;
import java.util.Collection;


@RunWith(Suite.class)
@Suite.SuiteClasses({
		 ServerLoadBalancerTest.ServerLoadBalanceTestParametrized.class ,ServerLoadBalancerTest.ServerLoadBalanceTestStatic.class
})
public class ServerLoadBalancerTest{

	public static class ServerLoadBalanceTestStatic extends ServerLoadBalancerBaseTest{

		@Test
		public void itCompiles() {
			assertThat(true, equalTo(true));
		}

		@Test
		public void balancingAServer_noVms_serverStaysEmpty() {
			Server theServer = a(server().withCapacity(1));

			balance(aListOfServersWith(theServer), anEmptyListOfVms());

			assertThat(theServer, hasLoadPercentageOf(0.0d));
		}

		@Test
		public void balancingOneServerWithOneSlotCapacity_andOneSlotVm_fillsTheServerWithTheVm() {
			Server theServer = a(server().withCapacity(1));
			Vm theVm = a(vm().ofSize(1));
			balance(aListOfServersWith(theServer), aListOfVmsWith(theVm));

			assertThat(theServer, hasLoadPercentageOf(100.0d));
			assertThat("the server should contain vm", theServer.contains(theVm));
		}

		@Test
		public void balancingOneServerWithTenSlotsCapacity_andOneSlotVm_fillTheServerWithTenPercent(){
			Server theServer = a(server().withCapacity(10));
			Vm theVm = a(vm().ofSize(1));
			balance(aListOfServersWith(theServer), aListOfVmsWith(theVm));

			assertThat(theServer, hasLoadPercentageOf(10.0d));
			assertThat("the server should contain vm", theServer.contains(theVm));

		}

		@Test
		public void balancingAServerWithEnoughRoom_getsFilledWithAllVms(){
			Server theServer = a(server().withCapacity(100));
			Vm theFirstVm = a(vm().ofSize(1));
			Vm theSecondVm = a(vm().ofSize(1));
			balance(aListOfServersWith(theServer), aListOfVmsWith(theFirstVm, theSecondVm));

			assertThat(theServer, hasVmsCountOf(2));
			assertThat("the server should contain vm", theServer.contains(theFirstVm));
			assertThat("the server should contain vm", theServer.contains(theSecondVm));

		}

		@Test
		public void aVm_shouldBeBalanced_onLessLoadedServerFirst(){
			Server lessLoadedServer = a(server().withCapacity(100).withCurrentLoadOf(45.0d));
			Server moreLoadedServer = a(server().withCapacity(100).withCurrentLoadOf(50.0d));
			Vm theVm = a(vm().ofSize(10));

			balance(aListOfServersWith(moreLoadedServer, lessLoadedServer), aListOfVmsWith(theVm));

			assertThat("the less loaded server should contain vm", lessLoadedServer.contains(theVm));

		}

		@Test
		public void balanceAServerWithNotEnoughRoom_shouldNotBeFilledWithAVm(){
			Server theServer = a(server().withCapacity(10).withCurrentLoadOf(90.0d));
			Vm theVm = a(vm().ofSize(2));
			balance(aListOfServersWith(theServer), aListOfVmsWith(theVm));

			assertThat("the less loaded server should not contain vm", !theServer.contains(theVm));
		}

	}


	@RunWith(value = Parameterized.class)
	public static class ServerLoadBalanceTestParametrized extends ServerLoadBalancerBaseTest{

		@Parameterized.Parameters public static Collection<Object[]> defaultDataToTest() {
			return Arrays.asList(
					new Object[][] {{0, 0, 0d, false}, {0, 1, 0d, false}, {1, 0, 0d, true}, {1, 1, 100.0d, true}, {1, 2, 0d, false}, {2, 1, 50.0d, true}, {2, 2, 100.0d, true}});
		}

		@Parameterized.Parameter(value = 0) public int inputServerCapacity;

		@Parameterized.Parameter(value = 1) public int inputVmSize;

		@Parameterized.Parameter(value = 2) public double expectedLoadPercentage;

		@Parameterized.Parameter(value = 3) public boolean expectedServerContainsVm;

		@Test public void balance_serversAndVms() {
			Server theServer = a(server().withCapacity(inputServerCapacity));
			Vm theVm = a(vm().ofSize(inputVmSize));
			balance(aListOfServersWith(theServer),aListOfVmsWith(theVm));

			assertThat(theServer.contains(theVm), is(expectedServerContainsVm));
			assertThat(theServer, hasLoadPercentageOf(expectedLoadPercentage));
		}

	}
}
