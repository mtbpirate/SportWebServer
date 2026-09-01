package org.pirate.sportwebserver.dto;

public class Bike 
{
	public int idbike;
	public int idsportart;
	public String text;
	public String gueltig_von;
	public String gueltig_bis;
	public float gewicht;
	public float cr;
	public float cwa;
	
	// Getter und Setter
	public int getIdbike() {
		return idbike;
	}
	
	public void setIdbike(int idbike) {
		this.idbike = idbike;
	}
	
	public int getIdsportart() {
		return idsportart;
	}
	
	public void setIdsportart(int idsportart) {
		this.idsportart = idsportart;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getGueltig_von() {
		return gueltig_von;
	}
	
	public void setGueltig_von(String gueltig_von) {
		this.gueltig_von = gueltig_von;
	}
	
	public String getGueltig_bis() {
		return gueltig_bis;
	}
	
	public void setGueltig_bis(String gueltig_bis) {
		this.gueltig_bis = gueltig_bis;
	}
	
	public float getGewicht() {
		return gewicht;
	}
	
	public void setGewicht(float gewicht) {
		this.gewicht = gewicht;
	}
	
	public float getCr() {
		return cr;
	}
	
	public void setCr(float cr) {
		this.cr = cr;
	}
	
	public float getCwa() {
		return cwa;
	}
	
	public void setCwa(float cwa) {
		this.cwa = cwa;
	}
}
