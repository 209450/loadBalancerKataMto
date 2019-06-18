package edu.iis.mto.serverloadbalancer;

import static edu.iis.mto.serverloadbalancer.CurrentLoadPercentageMatcher.hasLoadPercentageOf;
import static edu.iis.mto.serverloadbalancer.ServerBuilder.server;
import static edu.iis.mto.serverloadbalancer.ServerVmsCountMatcher.hasVmsCountOf;
import static edu.iis.mto.serverloadbalancer.VmBuilder.vm;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;

import java.util.*;
import java.util.stream.Collectors;

@RunWith(Suite.class)
@Suite.SuiteClasses({
		 ServerLoadBalancerTest.ServerLoadBalanceTestParametrized.class, ServerLoadBalancerTest.ServerLoadBalanceTestStatic.class,
		ServerLoadBalancerTest.ServerLoadBalanceTestParametrizedMultipleServersAndVms.class
})
public class ServerLoadBalancerTest  extends ServerLoadBalancerBaseTest{

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

		@Test
		public void balance_serversAndVms() {
			Server theServer = a(server().withCapacity(inputServerCapacity));
			Vm theVm = a(vm().ofSize(inputVmSize));
			balance(aListOfServersWith(theServer),aListOfVmsWith(theVm));

			assertThat(theServer.contains(theVm), is(expectedServerContainsVm));
			assertThat(theServer, hasLoadPercentageOf(expectedLoadPercentage));
		}

	}

	@RunWith(value = Parameterized.class)
	public static class ServerLoadBalanceTestParametrizedMultipleServersAndVms extends ServerLoadBalancerBaseTest{

		@Parameterized.Parameters public static Collection<Object[]> defaultDataToTest() {
			return Arrays.asList(
					new Object[][] {{(List.of(0,1,2)), List.of(0), List.of(0d,0d,0d), List.of(false,true,false)},
									{List.of(0,1,2), List.of(0,1,2),List.of(0d,100d,100d,0d,100d,0d,0d,100d,0d), List.of(false,false,false,true,true,false,false,false,true)},
									{List.of(0,1,2,3), List.of(0,3), List.of(0d,0d,0d,100d,0d,0d,0d,0d), List.of(false,false,true,false,false,false,false,true)}});
		}

		public static final ArrayList<ServerToTest> serverToTestArrayList = new ArrayList<>(
				List.of(new ServerToTest(0),new ServerToTest(1),new ServerToTest(2),new ServerToTest(3))
		);

		public static final ArrayList<VmToTest> vmToTestArrayList = new ArrayList<>(
				List.of(new VmToTest(0),new VmToTest(1),new VmToTest(2),new VmToTest(3))
		);

		@Parameterized.Parameter(value = 0) public List<Integer> inputListIndexOfServerToTest;

		@Parameterized.Parameter(value = 1) public List<Integer> inputIndexOfVmToTest;

		@Parameterized.Parameter(value = 2) public List<Double> expectedLoadPercentage;

		@Parameterized.Parameter(value = 3) public List<Boolean> expectedServerContainsVm;

		@Test
		public void balance_MultipleServersAndVms() {
			ArrayList<ServerToTest> serversToTest = new ArrayList<>();
			ArrayList<VmToTest> vmsToTest = new ArrayList<>();

			LinkedList<Double> expectedLoadPercentageLinkedList = new LinkedList<>(expectedLoadPercentage);
			LinkedList<Boolean> expectedServerContainsVmLinkedList = new LinkedList<>(expectedServerContainsVm);


			inputListIndexOfServerToTest.forEach(index->{
				serversToTest.add(serverToTestArrayList.get(index).makeCopy());
			});

			inputIndexOfVmToTest.forEach(index->{
				vmsToTest.add(vmToTestArrayList.get(index).makeCopy());
			});

			Server[] servers =  serversToTest.stream().map(i->i.server).toArray(Server[]::new);
			Vm[] vms = vmsToTest.stream().map(i->i.vm).toArray(Vm[]::new);

			balance(servers,vms);

			//LoadPercentage Assert
            for (Server server : servers) {
                assertThat(server, hasLoadPercentageOf(expectedLoadPercentageLinkedList.remove()));

            }

            //ServerContainsVm Assert
            for (Server server : servers) {
                for (Vm vm : vms) {
                    assertThat(server.contains(vm), is(expectedServerContainsVmLinkedList.remove()));
                }
            }


		}

	}

	private static class ServerToTest extends ServerLoadBalancerBaseTest{

		Server server;
		int serverCapacity;

		ServerToTest(int serverCapacity) {
			this.serverCapacity = serverCapacity;

			server = a(server().withCapacity(serverCapacity));
		}

		ServerToTest makeCopy(){
			return new ServerToTest(serverCapacity);
		}

	}

	private static class VmToTest extends ServerLoadBalancerBaseTest {

		Vm vm;
		int vmSize;

		public VmToTest(int vmSize) {
			this.vmSize = vmSize;

			vm = a(vm().ofSize(vmSize));
		}

		VmToTest makeCopy(){
			return new VmToTest(vmSize);
		}


	}

}
