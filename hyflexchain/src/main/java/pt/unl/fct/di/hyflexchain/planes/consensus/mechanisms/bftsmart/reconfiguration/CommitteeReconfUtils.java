package pt.unl.fct.di.hyflexchain.planes.consensus.mechanisms.bftsmart.reconfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import pt.unl.fct.di.hyflexchain.planes.consensus.committees.bft.BftCommittee;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.Address;
import pt.unl.fct.di.hyflexchain.planes.network.Host;

public class CommitteeReconfUtils {
    
    public static final File DEFAULT_CONFIG = new File("config");
    public static final File DEFAULT_BASE_CONFIG = new File(DEFAULT_CONFIG, "base-config");

    public static void createBftsmartConfigFiles(BftCommittee committee,
        Map<Address, Host> committeeMembers) throws IOException
    {
        createBftsmartConfigFiles(committee, committeeMembers, DEFAULT_BASE_CONFIG, DEFAULT_CONFIG);
    }

    public static void createBftsmartConfigFiles(BftCommittee committee,
        Map<Address, Host> committeeMembers,
        File baseConfigsFolder, File destFolder) throws IOException
    {
        createBftsmartHostsConfig(committee, committeeMembers, destFolder);
        createBftsmartSystemConfig(committee, baseConfigsFolder, destFolder);
        new File(destFolder, "currentView").delete();
    }

    private static void createBftsmartHostsConfig(BftCommittee committee,
        Map<Address, Host> committeeMembers, File destFolder) throws FileNotFoundException
    {
        try (
            PrintStream out = new PrintStream(new File(destFolder, "hosts.config"))
        ) {
            var it = committee.getCommittee().iterator();
            for (int i = 0; it.hasNext(); i++)
            {
                var node = committeeMembers.get(it.next());
                out.printf("%d %s 20000 20001\n", i, node.host());
            }

            out.flush();
        }
    }

    private static void createBftsmartSystemConfig(BftCommittee committee, File baseConfigsFolder, File destFolder) throws IOException
    {
        var baseConfigFile = new File(baseConfigsFolder, "system.config");
        var newConfigFile = new File(destFolder, "system.config");

        Files.copy(
            baseConfigFile.toPath(),
            newConfigFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        );
        
        try (

            PrintStream out =new PrintStream(new FileOutputStream(newConfigFile, true))
        ) {
            int size = committee.size();
            int f = committee.getBftCriteria().getF();

            // system.servers.num = size
            // system.servers.f = f
            // system.initial.view = seq(0..size-1)

            out.println("system.servers.num = " + size);
            out.println("system.servers.f = " + f);

            out.print("system.initial.view = ");

            for (int i = 0; i < size - 1; i++)
            {
                out.print(i);
                out.print(',');
            }
            out.println(size - 1);

            out.flush();
        }
    }

}
