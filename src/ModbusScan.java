import java.io.IOException;

import com.focus_sw.fieldtalk.BusProtocolException;
import com.focus_sw.fieldtalk.MbusMasterFunctions;
import com.focus_sw.fieldtalk.MbusTcpMasterProtocol;


public class ModbusScan {
	MbusTcpMasterProtocol mbus;
	
	
	protected boolean hitNetworkAddress(int networkAddress) {
		boolean b[] = new boolean[1];
		short s[] = new short[5];
		
		try { 
			//mbus.readCoils(networkAddress, 0, b);
			mbus.readInputRegisters(networkAddress, 1000, s);
			
			char c = (char) (s[0]&0xff);
			System.err.println("");
			System.err.println("# Likely APRS World serial number " + c + "" + s[1] + " @ address " + networkAddress);
			System.err.println("# with hardware model=" + (char) (s[3]&0xff) + " hardware version=" + (char) (s[4]&0xff)
					+ " software model=" + (char) (s[5]&0xff) + " software version=" + (char) (s[6]&0xff) );

			
//			System.err.println("# 1000=" + s[0] + " 10001=" + s[1]);
			
		} catch ( BusProtocolException bpe ) {
//			System.err.println("# BusProtocolException: " + bpe);
			return false;
		} catch ( IOException ioe ) {
//			System.err.println("# IOException: " + ioe);
			return false;
		} catch ( Exception e ) {
//			System.err.println("# Exception: " + e);
			return false;
		}
		
		return true;
	}
	
	public void run(String hostname, int port, int networkAddressStart, int networkAddressEnd) throws IOException {
		System.err.println("# scanning " + hostname + ":" + port);
		
		/* open modbus connection to host */
		mbus = new MbusTcpMasterProtocol();
		mbus.configureCountFromZero();
		mbus.setRetryCnt(0);
		mbus.setTimeout(1000);
		mbus.setPort(port);
		
		System.err.println("# Opening Modbus TCP connection");
		mbus.openProtocol(hostname);
		
		
		System.err.println("# Starting scan");
		System.err.println("# Network Address: ");
		for ( int networkAddress=networkAddressStart ; networkAddress<networkAddressEnd ; networkAddress++ ) {
			if ( (networkAddress % 10)==0 ) {
				System.err.print("\n(" + networkAddress + ") ");
			}
			
	
			boolean found = hitNetworkAddress(networkAddress);
			
			if ( found ) {
				System.err.println("");
				System.err.println("# Something found at: " + networkAddress);
			} else {
				System.err.print(".");
			}
		}
		System.err.println("# Done");
	
		
		
		if ( mbus.isOpen() ) {
			System.err.println("# Closing Modbus TCP connection");
			mbus.closeProtocol();
		}
			
		
	}
	
	
	public static void main(String[] args) {
		System.err.println("# ModbusScan 2014-11-15 (precision)");
		
		if ( args.length < 2 ) {
			System.err.println("usage: ModbusScan hostname part");
			System.exit(1);
		}
		
		String hostname=args[0];
		int port = Integer.parseInt(args[1]);
		
		int networkAddressStart=1;
		int networkAddressEnd=254;
		
		if ( args.length >= 3 ) {
			networkAddressStart = Integer.parseInt(args[2]);
		}
		
		if ( args.length >= 4 ) {
			networkAddressEnd = Integer.parseInt(args[3]);
		}
		
		try { 
			new ModbusScan().run(hostname, port, networkAddressStart, networkAddressEnd);
		} catch ( Exception e ) {
			System.err.println("# Exception while scanning: ");
			System.err.println(e.toString());
			e.printStackTrace();
		}

	}

}
