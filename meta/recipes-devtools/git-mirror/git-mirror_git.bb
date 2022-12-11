DESCRIPTION = "A small utility that allows to mirror external repositories to GitLab, GitHub and possible more."
SECTION = "git"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c6759b780e1a42d01552c4413d8534f3"

SRC_URI = "git://github.com/bachp/git-mirror.git;protocol=https;branch=master"
SRCREV = "1935fb3173d562e7db84a83777b76374df9e38cd"

S = "${WORKDIR}/git"

inherit cargo-vendor pkgconfig

DEPENDS = "openssl"

export OPENSSL_NO_VENDOR = "1"
