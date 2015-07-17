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
package com.francetelecom.clara.cloud.paas.constraint;

/**
 * This class contains per tenant projection customization rules. 
 * They allow to override default constraints for a specific tenant. 
 * 
 * max core=8
 * Max SpeecintRate2006=80
 * max RAM = 64GB
 * Max IO=1 GBPS
 * HA
 * DPM ?
 * NAS storage = SMALL (50GB), MEDIUM (50-300 MB), LARGE (300-1000 MB), XLARGE (> 1000 MB)
 * 
 * "Il n’y a pas de limitation explicite du nombre de VM maximum par serveur physique. L’exploitant réalise cette consolidation de manière à pouvoir respecter les engagements de qualité de service contractuel qu’il doit assurer. 
 * Pour le MOE, il est recommandé de limiter la taille des VM à 2 vCPUs et de privilégier la scalabilité horizontale de l’application."
 * 
 * http://private-url.elpaaso.org/sites/DT-DDSI/atl/Public/tuning_os_S1F2.doc
 * 4.2.1	Nombre de CPU Virtuels
Avec VMWare, le nombre de CPU virtuels (VCPU) attribué au serveur virtuel peut être: 1, 2 ou 4
Pour une application avec une charge faible (moins de 100 utilisateurs simultanés, moins de 500 hits / s), privilégier une VM mono CPU. (puissance de 20KTpmc pour un Ecoserveur LS41)
Au-delà de cette charge, passer sur des VM avec 2 VCPU pour une puissance de 40KTpmc.
Les serveurs virtuels avec 4 VCPU ne sont pas recommandés. Pour un besoin supérieur à 40Ktpmc, il faut privilégier la scalabilité horizontale (ferme de serveurs).

 
 
 */
public class TenantCustomizationRule {

	//
	//
	//	On doit ajuster la mémoire au plus juste par un dimensionnement fin en fonction des besoins qui sont : 
	//		Kernel Linux, hors buffer & cache système : 32M 
	//		PLI Standards d’exploitation : Selon PLI installés. Compter 128M
	//		Apache : compter 500K multiplié par le nombre maximum de processus Apache (Cf. Paramètre MaxClients dans la section sur le tuning Apache), si les préconisations de tuning Apache de ce document ont bien été appliquées. Pour MaxClients = 256, compter donc 128M
	//		Environ 1,3 fois la taille de la JAVA heap (cf. paramètre MAX_HEAP_SIZE dans la section sur le tuning de la JVM). Si il existe plusieurs JVM sur le même serveur, faire la somme des MAX_HEAP_SIZE*1,3.
	//		Marge de fonctionnement pour pouvoir exécuter les commandes d’administration, avoir assez de RAM pour les buffers systèmes: ajouter 20%.
	//
	
	private int linuxKernelMemoryMb = 32;
	
	private int standardOpsPackagesMemoryMb =128;
	
	private int apacheMemoryKbPerClient = 500;
	
	private int percentVmMarging = 20;
	
	private double jvmMaxHeapOsRatio = 1.3d;
	
	private int mysqlDefaultMemoryMb = 512;
	
	// Global architectural limitations
	// -------------------------------------------------
	public int minMemoryMbPerVm = 512;
	
	
	public int maxMemoryMbPerVm = 2048;

	public int minMemoryMbPerWas = 128;
	public int maxMemoryMbPerWas = 1536;

	private int maxWasPerVm = 4;
	public int maxDataBaseConnectionNumber = 100;
	public int maxSessionPerWas = 4000;
	
	public int brokerMemoryMb = 128;

	public int getMinMemoryMbPerVm() {
		return minMemoryMbPerVm;
	}
	public void setMinMemoryMbPerVm(int minMemoryMbPerVm) {
		this.minMemoryMbPerVm = minMemoryMbPerVm;
	}
	public int getMaxMemoryMbPerVm() {
		return maxMemoryMbPerVm;
	}
	public void setMaxMemoryMbPerVm(int maxMemoryMbPerVm) {
		this.maxMemoryMbPerVm = maxMemoryMbPerVm;
	}
	public int getMinMemoryMbPerWas() {
		return minMemoryMbPerWas;
	}
	public void setMinMemoryMbPerWas(int minMemoryMbPerWas) {
		this.minMemoryMbPerWas = minMemoryMbPerWas;
	}
	public int getMaxMemoryMbPerWas() {
		return maxMemoryMbPerWas;
	}
	public void setMaxMemoryMbPerWas(int maxMemoryMbPerWas) {
		this.maxMemoryMbPerWas = maxMemoryMbPerWas;
	}
	public int getMaxWasPerVm() {
		return maxWasPerVm;
	}
	public void setMaxWasPerVm(int maxWasPerVm) {
		this.maxWasPerVm = maxWasPerVm;
	}
	public int getMaxDataBaseConnectionNumber() {
		return maxDataBaseConnectionNumber;
	}
	public void setMaxDataBaseConnectionNumber(int maxDataBaseConnectionNumber) {
		this.maxDataBaseConnectionNumber = maxDataBaseConnectionNumber;
	}
	public int getMaxSessionPerWas() {
		return maxSessionPerWas;
	}
	public void setMaxSessionPerWas(int maxSessionPerWas) {
		this.maxSessionPerWas = maxSessionPerWas;
	}

    /** Size to allocated the linux kernel */
    public int getLinuxKernelMemoryMb() {
        return linuxKernelMemoryMb;
    }

    public void setLinuxKernelMemoryMb(int linuxKernelMemoryMb) {
        this.linuxKernelMemoryMb = linuxKernelMemoryMb;
    }

    public int getStandardOpsPackagesMemoryMb() {
        return standardOpsPackagesMemoryMb;
    }

    public void setStandardOpsPackagesMemoryMb(int standardOpsPackagesMemoryMb) {
        this.standardOpsPackagesMemoryMb = standardOpsPackagesMemoryMb;
    }

    public int getApacheMemoryKbPerClient() {
        return apacheMemoryKbPerClient;
    }

    public void setApacheMemoryKbPerClient(int apacheMemoryKbPerClient) {
        this.apacheMemoryKbPerClient = apacheMemoryKbPerClient;
    }

    /** Percentage of marging for OS-level buffers */
    public int getPercentVmMarging() {
        return percentVmMarging;
    }

    public void setPercentVmMarging(int percentVmMarging) {
        this.percentVmMarging = percentVmMarging;
    }

    /** ratio add to convert JVM max heap into OS memory consumption. */
    public double getJvmMaxHeapOsRatio() {
        return jvmMaxHeapOsRatio;
    }

    public void setJvmMaxHeapOsRatio(double jvmMaxHeapOsRatio) {
        this.jvmMaxHeapOsRatio = jvmMaxHeapOsRatio;
    }

    /** Default mysql daemon memory size, until finding related tuning guide */
    public int getMysqlDefaultMemoryMb() {
        return mysqlDefaultMemoryMb;
    }

    public void setMysqlDefaultMemoryMb(int mysqlDefaultMemoryMb) {
        this.mysqlDefaultMemoryMb = mysqlDefaultMemoryMb;
    }

	/**
	 * @return the brokerMemoryMb
	 */
	public int getBrokerMemoryMb() {
		return brokerMemoryMb;
	}

	/**
	 * @param brokerMemoryMb
	 *            the brokerMemoryMb to set
	 */
	public void setBrokerMemoryMb(int brokerMemoryMb) {
		this.brokerMemoryMb = brokerMemoryMb;
	}
}
