#! /usr/bin/env groovy

//tag::dependencies[]
@GrabResolver(name='puravida', root="https://dl.bintray.com/puravida-software/repo" )
@Grab(group = 'com.google.code.findbugs', module = 'jsr305', version = '3.0.2')
@Grab(group = 'com.puravida.groogle', module = 'groogle-sheet', version = '1.1.1')
@Grab('org.apache.commons:commons-csv:1.5')
@GrabConfig(systemClassLoader=true)

import com.google.api.services.sheets.v4.SheetsScopes
import com.puravida.groogle.GroogleScript
import com.puravida.groogle.SheetScript
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVFormat

//end::dependencies[]

//tag::cli[]
cli = new CliBuilder(usage: '-i -s spreadSheetId g2d|d2g')
cli.with {
    h(longOpt: 'help',    args:0,'Usage Information', required: false)
    s(longOpt: 'spreadSheet', args:1, argName:'spreadSheetId', 'El id de la hoja a usar', required: true)
}
options = cli.parse(args)
if (!options) return
if (options.h || options.arguments().size()!=1) {
    cli.usage()
    return
}



//tag::login[]
clientSecret = new File('credentials.json').newInputStream()
groogleScript = new GroogleScript('new test sheet', clientSecret,[SheetsScopes.SPREADSHEETS])
sheetScript = new SheetScript(groogleScript: groogleScript)
//end::login[]

//tag::schemas[]
sheetScript.withSpreadSheet options.spreadSheet, { spreadSheet ->   

    sheets.each{ gSheet ->                                          
        String hojaId = gSheet.properties.title
        println "Google Sheet Title: $hojaId"
	
        withSheet hojaId, { sheet ->                                
	    
	    int idx = 13
	    def FILE_HEADER = ['IDENTIFIER','AMOUNT']   
	    def fileName = 'data.csv'
            def rows = readRows("B$idx", "J$idx").first()    
	    def DATA = []
	    def WRITERS = []
	    DATA.add(rows[0])
	    DATA.add(rows[8])
	    WRITERS.add(DATA)
	    while (rows) {
                    idx++
                    rows = readRows("B$idx", "J${idx + 100}")
		    if(rows != null) {
			    DATA = []
			    DATA.add(rows[0][0])
			    DATA.add(rows[0][8])
	    		    WRITERS.add(DATA)
		    }
            }
	    println "DATA: $WRITERS"
	    
	    new File(fileName).withWriter { fileWriter ->
	    	CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT)
	    	csvFilePrinter.printRecord(FILE_HEADER)
	    	WRITERS.each { i ->
		    	csvFilePrinter.printRecord(i)
	    	}
	    }
        }
    }
}
//end::schemas[]

