package crash.ref.commands

import com.francetelecom.clara.cloud.service.OpsService
import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.command.InvocationContext

@Usage("paas ssample")
class ssample {

    // Properties command
    @Usage("ssample")
    @Command
    public void gogo(InvocationContext<Map> context) {
        def OpsService opsService = context.attributes.beans["opsService"];
        out << " build user : " << opsService.getBuildUser() << "\n";
    }

}
