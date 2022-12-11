#
# Copyright OpenEmbedded Contributors
#
# SPDX-License-Identifier: MIT
#

##
## Purpose:
## This class is used to automatically create a vendor
## tarball for a cargo packages.
##
## Implementation:
## This class adds an additional do_cargo_vendor task.
## It will first try to download an existing vendor tarball from a mirror.
## If one exists it will be extracted to where bitbake expects the vendor directory.
## If it doesn't exist cargo vendor is executed generat the vondor directory and the resulting content is put into a tarball that
## can be uploaded to a mirror.
##
## The do_cargo_vendor task needs network access to either fetch the tarball or fetch the sources via cargo_vendor.
## It is similar in that sense to the do_fetch task

inherit cargo

CARGO_VENDOR_DL_DIR ?= "${DL_DIR}"
CARGO_VENDOR_DL_DIR[doc] = "Directory where cargo vendor tarballs will be stored."

CARGO_VENDOR_FLAGS = "-v --manifest-path=${MANIFEST_PATH} ${CARGO_VENDORING_DIRECTORY}"
CARGO_VENDOR_TARRBALL = "cargo-vendor-${BPN}-${PV}-${CARGO_VENDOR_HASH}.tar.gz"
CARGO_VENDOR_URL = "file://${CARGO_VENDOR_DL_DIR}"

oe_cargo_vendor () {
    export RUSTFLAGS="${RUSTFLAGS}"
    bbnote "cargo = $(which ${CARGO})"
    bbnote "${CARGO} vendor ${CARGO_VENDOR_FLAGS} $@"
    "${CARGO}" vendor ${CARGO_VENDOR_FLAGS} "$@"
}

oe_cargo_vendor_create_tarball () {
    bbnote  tar -caf "${CARGO_VENDOR_DL_DIR}/${CARGO_VENDOR_TARRBALL}" -C "${CARGO_VENDORING_DIRECTORY}" .
    tar -caf "${CARGO_VENDOR_DL_DIR}/${CARGO_VENDOR_TARRBALL}" -C "${CARGO_VENDORING_DIRECTORY}" .
    touch "${CARGO_VENDOR_DL_DIR}/${CARGO_VENDOR_TARRBALL}.done"
}

oe_cargo_vendor_extract_tarball () {
    bbnote  tar -xaf "${CARGO_VENDOR_DL_DIR}/${CARGO_VENDOR_TARRBALL}" -C "${CARGO_VENDORING_DIRECTORY}"
    tar -xaf "${CARGO_VENDOR_DL_DIR}/${CARGO_VENDOR_TARRBALL}" -C "${CARGO_VENDORING_DIRECTORY}"
}

do_cargo_vendor[depends] = "cargo-native:do_populate_sysroot"
do_cargo_vendor[dirs] = "${CARGO_VENDORING_DIRECTORY}"
do_cargo_vendor[network] = "1"
do_cargo_vendor[doc] = "Generate a cargo vendor directory by either downloading it from a mirror or generating it via cargo vendor"
python do_cargo_vendor() {
    cargo_lock = os.path.join(os.path.dirname(d.getVar("MANIFEST_PATH")), "Cargo.lock")
    import hashlib

    # We use the hash of the Cargo.lock file to generate a unique tarball name
    cargo_lock_hash = None
    with open(cargo_lock) as f:
        content = f.read().encode("utf-8")
        cargo_lock_hash = hashlib.sha256(content).hexdigest()
        d.setVar("CARGO_VENDOR_HASH", cargo_lock_hash)

    if not cargo_lock_hash:
        bb.error("%s doesn't exist, can't vendor dependencies" % cargo_lock)
    urls = ["%s/%s" %(d.getVar("CARGO_VENDOR_URL"), d.getVar("CARGO_VENDOR_TARRBALL"))]

    try:
        fetch = bb.fetch2.Fetch(urls, d)
        fetch.download()

        bb.note("Using alrady created vendor tarball")
        bb.build.exec_func("oe_cargo_vendor_extract_tarball", d)

    except bb.fetch2.BBFetchException as exc:
        bb.warn("Using Cargo.lock at: %s with sha256: %s" % (cargo_lock, cargo_lock_hash))
        bb.build.exec_func("oe_cargo_vendor", d)
        bb.build.exec_func("oe_cargo_vendor_create_tarball", d)

}
# Executeion after do_patch but before do_configure, allows to apply patches to the Cargo.toml and Cargo.lock before the vendoring happens.
# This can be worked round by patching Cargo.toml and Cargo.lock to point to patched sources instead.
addtask cargo_vendor after do_patch before do_configure
