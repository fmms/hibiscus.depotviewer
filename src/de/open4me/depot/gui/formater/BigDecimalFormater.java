package de.open4me.depot.gui.formater;

import java.math.BigDecimal;

import de.willuhn.jameica.gui.formatter.Formatter;

public class BigDecimalFormater implements Formatter
{


	private int nachkommastellen;

	public BigDecimalFormater(int nachkommastellen)
	{
		this.nachkommastellen = nachkommastellen;
	}

	public String format(Object o)
	{
		if (o == null || !(o instanceof BigDecimal))
			return "";
		return 	  ((BigDecimal) o).setScale(nachkommastellen, BigDecimal.ROUND_HALF_UP).toPlainString();
	}

}
