package pt.ulisboa.tecnico.softeng.broker.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import pt.ulisboa.tecnico.softeng.activity.domain.Activity;
import pt.ulisboa.tecnico.softeng.activity.domain.ActivityOffer;
import pt.ulisboa.tecnico.softeng.activity.domain.ActivityProvider;
import pt.ulisboa.tecnico.softeng.activity.interfaces.BankInterface;
import pt.ulisboa.tecnico.softeng.activity.interfaces.TaxInterface;
import pt.ulisboa.tecnico.softeng.bank.domain.Account;
import pt.ulisboa.tecnico.softeng.bank.domain.Bank;
import pt.ulisboa.tecnico.softeng.bank.domain.Client;
import pt.ulisboa.tecnico.softeng.hotel.domain.Hotel;
import pt.ulisboa.tecnico.softeng.hotel.domain.Room;
import pt.ulisboa.tecnico.softeng.hotel.domain.Room.Type;
import pt.ulisboa.tecnico.softeng.tax.dataobjects.InvoiceData;

@RunWith(JMockit.class)
public class AdventureProcessMethodTest {
	private static final String BROKER_IBAN = "BROKER_IBAN";
	private static final String NIF_AS_BUYER = "buyerNIF";
	private static final String NIF_AS_SELLER = "sellerNIF";
	private static final String NIF = "123456789";
	private final LocalDate begin = new LocalDate(2016, 12, 19);
	private final LocalDate end = new LocalDate(2016, 12, 21);
	private Broker broker;
	private String IBAN;

	@Before
	public void setUp() {
		this.broker = new Broker("BR01", "eXtremeADVENTURE", NIF_AS_SELLER, NIF_AS_BUYER, BROKER_IBAN);

		Bank bank = new Bank("Money", "BK01");
		Client client = new Client(bank, "António");
		Account account = new Account(bank, client);
		this.IBAN = account.getIBAN();
		account.deposit(1000);

		Hotel hotel = new Hotel("XPTO123", "Paris", "NIF", "IBAN", 20.0, 30.0);
		new Room(hotel, "01", Type.SINGLE);

		ActivityProvider provider = new ActivityProvider("XtremX", "ExtremeAdventure", "NIF", "IBAN");
		Activity activity = new Activity(provider, "Bush Walking", 18, 80, 10);
		new ActivityOffer(activity, this.begin, this.end, 30);
		new ActivityOffer(activity, this.begin, this.begin, 40);
	}

	@Test
	public void success(@Mocked final TaxInterface taxInterface, @Mocked final BankInterface bankInterface) {
		new Expectations() {
			{
				BankInterface.processPayment(this.anyString, this.anyInt);

				TaxInterface.submitInvoice((InvoiceData) this.any);
			}
		};

		Adventure adventure = new Adventure(this.broker, this.begin, this.end,
				new pt.ulisboa.tecnico.softeng.broker.domain.Client(this.broker, this.IBAN, NIF, 20), 300);

		adventure.process();
		adventure.process();
		adventure.process();

		assertEquals(Adventure.State.CONFIRMED, adventure.getState());
		assertNotNull(adventure.getPaymentConfirmation());
		assertNotNull(adventure.getRoomConfirmation());
		assertNotNull(adventure.getActivityConfirmation());
	}

	@Test
	public void successNoHotelBooking(@Mocked final TaxInterface taxInterface,
			@Mocked final BankInterface bankInterface) {
		new Expectations() {
			{
				BankInterface.processPayment(this.anyString, this.anyInt);

				TaxInterface.submitInvoice((InvoiceData) this.any);
			}
		};

		Adventure adventure = new Adventure(this.broker, this.begin, this.begin,
				new pt.ulisboa.tecnico.softeng.broker.domain.Client(this.broker, this.IBAN, NIF, 20), 300);

		adventure.process();
		adventure.process();

		assertEquals(Adventure.State.CONFIRMED, adventure.getState());
		assertNotNull(adventure.getPaymentConfirmation());
		assertNotNull(adventure.getActivityConfirmation());
	}

	@After
	public void tearDown() {
		Bank.banks.clear();
		Hotel.hotels.clear();
		ActivityProvider.providers.clear();
		Broker.brokers.clear();
	}
}