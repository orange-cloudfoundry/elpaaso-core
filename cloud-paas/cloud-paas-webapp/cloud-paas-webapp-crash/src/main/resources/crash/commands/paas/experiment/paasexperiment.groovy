/**
 * Copyright (C) 2015 Orange
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package crash.commands.paas.experiment

import com.francetelecom.clara.cloud.crash.PaaSCRaSHCommand
import com.francetelecom.clara.cloud.service.OpsService
import org.crsh.cli.Command
import org.crsh.cli.Man
import org.crsh.cli.Usage

@Usage("paas ops *EXPERIMENTALS* commands")
class paasexperiment  extends PaaSCRaSHCommand {
    @Usage("show paas informations")
    @Man("show server date, show the paas (service layer) version, build timestamp, and build user.")
    @Command
    public void info() {
        def OpsService opsService = getOpsService();
        out << " server date : " << opsService.getServerDate() << "\n";
        out << " PaaS [service layer]\n";
        out << " version    : " << opsService.getBuildVersion() << "\n";
        out << " build date : " << opsService.getBuildDate() << "\n";
        out << " build user : " << opsService.getBuildUser() << "\n";
    }

}
