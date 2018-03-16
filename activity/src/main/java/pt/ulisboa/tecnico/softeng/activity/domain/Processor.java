package pt.ulisboa.tecnico.softeng.activity.domain;

import java.util.HashSet;
import java.util.Set;

import pt.ulisboa.tecnico.softeng.activity.exception.RemoteAccessException;
import pt.ulisboa.tecnico.softeng.activity.interfaces.BankInterface;
import pt.ulisboa.tecnico.softeng.activity.interfaces.TaxInterface;
import pt.ulisboa.tecnico.softeng.bank.exception.BankException;
import pt.ulisboa.tecnico.softeng.tax.dataobjects.InvoiceData;
import pt.ulisboa.tecnico.softeng.tax.exception.TaxException;

public class Processor {
	private final Set<Booking> bookingToProcess = new HashSet<>();

	public void submitBooking(Booking booking) {
		this.bookingToProcess.add(booking);
		processInvoices();
	}

	private void processInvoices() {
		Set<Booking> failedToProcess = new HashSet<>();
		for (Booking booking : this.bookingToProcess) {
			if (booking.getPaymentReference() == null) {
				try {
					booking.setPaymentReference(BankInterface.processPayment(booking.getNif(), booking.getAmount()));
				} catch (BankException | RemoteAccessException ex) {
					failedToProcess.add(booking);
					continue;
				}
			}
			InvoiceData invoiceData = new InvoiceData(booking.getProviderNif(), booking.getNif(), booking.getType(),
					booking.getAmount(), booking.getDate());
			try {
				booking.setInvoiceReference(TaxInterface.submitInvoice(invoiceData));
			} catch (TaxException | RemoteAccessException ex) {
				failedToProcess.add(booking);
			}
		}

		this.bookingToProcess.clear();
		this.bookingToProcess.addAll(failedToProcess);
	}

	public void clean() {
		this.bookingToProcess.clear();
	}

}
