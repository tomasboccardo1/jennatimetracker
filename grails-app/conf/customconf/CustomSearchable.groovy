package customconf

environments {
	production {
		searchable {
            //Aca hay que poner un path absoluto para los indices. debe existir previamente.
			compassConnection = new File('./').absolutePath
		}
	}
}