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
package crash.commands.paas

import com.francetelecom.clara.cloud.coremodel.Environment
import com.francetelecom.clara.cloud.crash.PaaSCRaSHCommand
import com.francetelecom.clara.cloud.service.OpsService
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto
import com.francetelecom.clara.cloud.services.dto.EnvironmentOpsDetailsDto
import com.francetelecom.clara.cloud.services.dto.LinkDto
import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Man
import org.crsh.cli.Usage

import java.text.SimpleDateFormat

@Usage("paas ops commands")
// only available with groovy 2.0 // @TypeChecked
class ops extends PaaSCRaSHCommand {
    public static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm")

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

    @Usage("count application command")
    @Command
    public void countApp() {
        out << getManageApplication().countApplications() << " application(s)";
    }

    @Usage("list environments command")
    @Command
    public void envs() {
        def List<EnvironmentDto> envs = getManageEnvironment().findEnvironments(0, 30, Environment.CREATION_DATE, "DESC");
        if (envs.size() == 0) {
            out << "no environment\n";
            return;
        }
        out << envs.size() << " environments :\n";

        for (EnvironmentDto envDto : envs) {
            if (null != envDto) {
                def statusBuilder = new StringBuilder().append(envDto.status);
                if (envDto.statusPercent != null) {
                    statusBuilder.append(" ").append(envDto.statusPercent).append("%")
                }
                if (envDto.statusMessage != null) {
                    statusBuilder.append(" : ").append(envDto.statusMessage)
                }
                def String envStatus = statusBuilder.toString()
                def String envComment = (envDto.comment != null ? envDto.comment : "(empty)");
                context.provide([
                        uid: envDto.uid,
                        owner: envDto.ownerName,
                        date: sdf.format(envDto.creationDate),
                        label: envDto.label,
                        status: envStatus,
                        comment: envComment
                ]);
            }
        }
    }

    @Usage("list environment OPS properties")
    @Man("to list environment properties, please select the good uid before using 'ops envs' command.")
    @Command
    public void env(@Usage("environment uid")
                    @Argument String envUid) {
        def EnvironmentOpsDetailsDto envOpsDetails = getManageEnvironment().findEnvironmentOpsDetailsByUID(envUid);
        if (envOpsDetails == null) {
            out << "no environment with this uid '" << envUid << "'\n";
            return;
        }
        out << "\nEnvironment [uid:" << envUid << "] OPS details :\n\n";
        context.provide([application: envOpsDetails.applicationLabel,
                         release: envOpsDetails.releaseVersion,
                         environment: envOpsDetails.label]);
        out.flush();
        String envOwner = envOpsDetails.ownerName + "(" + envOpsDetails.ownerId + ")";
        def envDetailsMap = [
                date: sdf.format(envOpsDetails.creationDate),
                owner: envOwner,
                logs: envOpsDetails.environmentOverallsLinkDto.url
        ]
        if (envOpsDetails.comment != null) {
            envDetailsMap.put("comment", envOpsDetails.comment);
        }
        context.provide(envDetailsMap);
        out.flush();

        def List<EnvironmentOpsDetailsDto.VMAccessDto> vmAccess = envOpsDetails.listVMAccesses();
        out << "\nEnvironment [uid:" << envUid << "] " << vmAccess.size() << " vm(s) :\n";
        for (EnvironmentOpsDetailsDto.VMAccessDto vmInfo : vmAccess) {
            context.provide([
                    ip: vmInfo.ip,
                    user: vmInfo.user,
                    password: vmInfo.password,
                    iaasId: vmInfo.iaasId,
                    hostname: vmInfo.hostname
            ]);
        }
        out.flush();

        def List<LinkDto> accessLinkDtosMap = envOpsDetails.getSpecificLinkDto(LinkDto.LinkTypeEnum.ACCESS_LINK);
        out << "\nEnvironment [uid:" << envUid << "] " << accessLinkDtosMap.size() << " access link(s) :\n";
        for (LinkDto link : accessLinkDtosMap) {
            context.provide([
                    type: link.linkType,
                    url: link.url.toExternalForm()
            ]);
        }
        out.flush();
    }

}
