package de.open4me.depot.abruf.hbci;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.open4me.depot.abruf.impl.BasisDepotAbruf;
import de.open4me.depot.abruf.impl.DepotAbrufFabrik;
import de.open4me.depot.abruf.utils.PropHelper;
import de.open4me.depot.abruf.utils.Utils;
import de.open4me.depot.hbcijobs.HBCIDepotBestandJob;
import de.open4me.depot.hbcijobs.HBCIDepotUmsatzJob;
import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobKontoauszug;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Ein Synchronize-Job fuer das Abrufen der Umsaetze eines Depot-Kontos.
 */
public class DVHBCISynchronizeJobDepotKontoauszug extends SynchronizeJobKontoauszug implements HBCISynchronizeJob
{
  /**
   * @see de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeJob#createHBCIJobs()
   */
  public AbstractHBCIJob[] createHBCIJobs() throws RemoteException, ApplicationException
  {
    Konto k              = (Konto) this.getContext(CTX_ENTITY);
    Boolean forceUmsatz  = (Boolean) this.getContext(CTX_FORCE_UMSATZ);
    
    SynchronizeOptions o = new SynchronizeOptions(k);
    
    List<AbstractHBCIJob> jobs = new ArrayList<AbstractHBCIJob>();

    if (o.getSyncKontoauszuege() || (forceUmsatz != null && forceUmsatz.booleanValue())) 
    {
		boolean supportDepotList = true;
		boolean supportDepotUmsatz = !Boolean.valueOf(k.getMeta(PropHelper.NURBESTAND, "false"));
		
		
		Logger.info("Support für Bestand/Umsätze: " + supportDepotList + "/" +  supportDepotUmsatz);
		
		// Einige Banken unterstützen per HBCI nur die Bestandabfrage
		// Hier können wir die Umsatzabfrage via Screen Screen Scraping erledigen 
		BasisDepotAbruf x = DepotAbrufFabrik.getDepotAbrufHBCI(k);
		
		// ggf. kann der Abruf über Screenscraping deaktiviert sein.
		// TODO Möglichkeit einbauen, dass Screenscraping auf vernünftige Weise zu deaktivieren
		//		if (!supportDepotUmsatz) {
		//				x = null;
		//		}
		
		// Bestimmen, ob die Erzeugung der Umsätze aus den Bestandsveränderungen aktiviert werden soll
		boolean simulateOrders =  (x == null) && !supportDepotUmsatz;

		// Bestand abrufen
		if (supportDepotList) {
	      	jobs.add(new HBCIDepotBestandJob(k, simulateOrders, x));
		}

		// Umsatz abrufen
		if (supportDepotUmsatz && x == null) {
	      	jobs.add(new HBCIDepotUmsatzJob(k));
		}

    }

    return jobs.toArray(new AbstractHBCIJob[jobs.size()]);
  }

  public static List<String> getProf(Konto k) throws RemoteException, ApplicationException {
	  	if (!Utils.hasRightKontoType(k)) {
	  		return null;
	  	}
		BasisDepotAbruf x = DepotAbrufFabrik.getDepotAbrufHBCI(k);
		if (x == null) {
			List<String> liste = Arrays.asList(new String[]{PropHelper.NURBESTANDINKLFORMAT});
			return liste;
		}
		return x.getPROP(k);
  }
}
