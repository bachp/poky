LICENSE = "GPL"
SECTION = "console/utils"
DESCRIPTION = "sed is a Stream EDitor."
PR = "r2"

DEPENDS = "gettext"

SRC_URI = "${GNU_MIRROR}/sed/sed-${PV}.tar.gz"

inherit autotools

do_install () {
	autotools_do_install
	install -d ${D}${base_bindir}
	mv ${D}${bindir}/sed ${D}${base_bindir}/sed.${PN}
}


pkg_postinst_${PN} () {
	update-alternatives --install ${base_bindir}/sed sed sed.${PN} 100
}


pkg_prerm_${PN} () {
	update-alternatives --remove sed sed.${PN}
}

BBCLASSEXTEND = "native"
