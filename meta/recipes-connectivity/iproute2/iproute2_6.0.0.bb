SUMMARY = "TCP / IP networking and traffic control utilities"
DESCRIPTION = "Iproute2 is a collection of utilities for controlling \
TCP / IP networking and traffic control in Linux.  Of the utilities ip \
and tc are the most important.  ip controls IPv4 and IPv6 \
configuration and tc stands for traffic control."
HOMEPAGE = "http://www.linuxfoundation.org/collaborate/workgroups/networking/iproute2"
SECTION = "base"
LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://COPYING;md5=eb723b61539feef013de476e68b5c50a \
                    file://ip/ip.c;beginline=3;endline=8;md5=689d691d0410a4b64d3899f8d6e31817"

DEPENDS = "flex-native bison-native iptables libcap"

SRC_URI = "${KERNELORG_MIRROR}/linux/utils/net/${BPN}/${BP}.tar.xz \
           file://0001-libc-compat.h-add-musl-workaround.patch \
           "

SRC_URI[sha256sum] = "523139e9e72aec996374fa2de74be4c53d2dd05589488934d21ff97bae19580a"

inherit update-alternatives bash-completion pkgconfig

PACKAGECONFIG ??= "tipc elf devlink"
PACKAGECONFIG[tipc] = ",,libmnl,"
PACKAGECONFIG[elf] = ",,elfutils,"
PACKAGECONFIG[devlink] = ",,libmnl,"
PACKAGECONFIG[rdma] = ",,libmnl,"
PACKAGECONFIG[selinux] = ",,libselinux"

IPROUTE2_MAKE_SUBDIRS = "lib tc ip bridge misc genl ${@bb.utils.filter('PACKAGECONFIG', 'devlink tipc rdma', d)}"

# CFLAGS are computed in Makefile and reference CCOPTS
#
EXTRA_OEMAKE = "\
    CC='${CC}' \
    KERNEL_INCLUDE=${STAGING_INCDIR} \
    DOCDIR=${docdir}/iproute2 \
    SUBDIRS='${IPROUTE2_MAKE_SUBDIRS}' \
    SBINDIR='${base_sbindir}' \
    LIBDIR='${libdir}' \
    CCOPTS='${CFLAGS}' \
"

do_configure:append () {
    sh configure ${STAGING_INCDIR}
    # Explicitly disable ATM support
    sed -i -e '/TC_CONFIG_ATM/d' config.mk
}

do_install () {
    oe_runmake DESTDIR=${D} install
    mv ${D}${base_sbindir}/ip ${D}${base_sbindir}/ip.iproute2
    install -d ${D}${datadir}
    mv ${D}/share/* ${D}${datadir}/ || true
    rm ${D}/share -rf || true
}

# The .so files in iproute2-tc are modules, not traditional libraries
INSANE_SKIP:${PN}-tc = "dev-so"

IPROUTE2_PACKAGES =+ "\
    ${PN}-devlink \
    ${PN}-genl \
    ${PN}-ifstat \
    ${PN}-ip \
    ${PN}-lnstat \
    ${PN}-nstat \
    ${PN}-rtacct \
    ${PN}-ss \
    ${PN}-tc \
    ${PN}-tipc \
    ${PN}-rdma \
"

PACKAGE_BEFORE_PN = "${IPROUTE2_PACKAGES}"
RDEPENDS:${PN} += "${PN}-ip"

FILES:${PN}-tc = "${base_sbindir}/tc* \
                  ${libdir}/tc/*.so"
FILES:${PN}-lnstat = "${base_sbindir}/lnstat \
                      ${base_sbindir}/ctstat \
                      ${base_sbindir}/rtstat"
FILES:${PN}-ifstat = "${base_sbindir}/ifstat"
FILES:${PN}-ip = "${base_sbindir}/ip.${PN} ${sysconfdir}/iproute2"
FILES:${PN}-genl = "${base_sbindir}/genl"
FILES:${PN}-rtacct = "${base_sbindir}/rtacct"
FILES:${PN}-nstat = "${base_sbindir}/nstat"
FILES:${PN}-ss = "${base_sbindir}/ss"
FILES:${PN}-tipc = "${base_sbindir}/tipc"
FILES:${PN}-devlink = "${base_sbindir}/devlink"
FILES:${PN}-rdma = "${base_sbindir}/rdma"

ALTERNATIVE:${PN}-ip = "ip"
ALTERNATIVE_TARGET[ip] = "${base_sbindir}/ip.${BPN}"
ALTERNATIVE_LINK_NAME[ip] = "${base_sbindir}/ip"
ALTERNATIVE_PRIORITY = "100"

ALTERNATIVE:${PN}-tc = "tc"
ALTERNATIVE_LINK_NAME[tc] = "${base_sbindir}/tc"
ALTERNATIVE_PRIORITY_${PN}-tc = "100"
