##
# Copyright (c) Members of the EGEE Collaboration. 2006-2010.
# See http://www.eu-egee.org/partners/ for details on the copyright holders.
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
##

name=slcs-client

version=2.0
release=1

# install options (like configure)
prefix=/usr/local
bindir=$(prefix)/bin
sysconfdir=$(prefix)/etc
datarootdir=$(prefix)/share
datadir=$(datarootdir)/slcs
mandir=$(datarootdir)/man
docdir=$(datarootdir)/doc/$(name)

# tmp
tmp_dir=$(CURDIR)/tmp

# RPM
spec_file = fedora/$(name).spec
rpmbuild_dir = $(CURDIR)/rpmbuild

# Debian
debbuild_dir = $(CURDIR)/debbuild


.PHONY: clean dist package install

all: package

clean:
	rm -rf target $(tmp_dir) *.tar.gz $(rpmbuild_dir) $(spec_file) *.rpm $(name) $(debbuild_dir) *.deb *.dsc

dist:
	@echo "Package the sources..."
	test ! -d $(tmp_dir) || rm -fr $(tmp_dir)
	mkdir -p $(tmp_dir)/$(name)-$(version)
	cp .classpath .project Makefile README.md pom.xml $(tmp_dir)/$(name)-$(version)
	cp -r fedora $(tmp_dir)/$(name)-$(version)
	cp -r debian $(tmp_dir)/$(name)-$(version)
	cp -r doc $(tmp_dir)/$(name)-$(version)
	cp -r src $(tmp_dir)/$(name)-$(version)
	test ! -f $(name)-$(version).tar.gz || rm $(name)-$(version).tar.gz
	tar -C $(tmp_dir) -czf $(name)-$(version).tar.gz $(name)-$(version)
	rm -fr $(tmp_dir)

package:
	@echo "Build package with maven"
	mvn -B package

bin-package:
	test -d target/$(name)-package || make package
	@echo "Package the binary..."
	test ! -f $(name)-$(version)-$(release).tar.gz || rm $(name)-$(version)-$(release).tar.gz
	tar -C target/$(name)-package -czf $(name)-$(version)-$(release).tar.gz bin etc share

install:
	test -d target/$(name)-package || make package
	@echo "Install binary in $(DESTDIR)$(prefix)"
	@echo " sysconfdir: $(DESTDIR)$(sysconfdir)"
	@echo " bindir: $(DESTDIR)$(bindir)"
	@echo " datadir: $(DESTDIR)$(datadir)"
	@echo " docdir: $(DESTDIR)$(docdir)"
	@echo " mandir: $(DESTDIR)$(mandir)"
	install -d $(DESTDIR)$(sysconfdir)/slcs
	install -m 0644 target/$(name)-package/etc/slcs/* $(DESTDIR)$(sysconfdir)/slcs
	install -d $(DESTDIR)$(bindir)
	install -m 0755 target/$(name)-package/bin/slcs-* $(DESTDIR)$(bindir)
	install -d $(DESTDIR)$(datadir)
	install -m 0644 target/$(name)-package/share/slcs/*.jar $(DESTDIR)$(datadir)
	install -d $(DESTDIR)$(mandir)/man1
	install -m 0644 target/$(name)-package/share/man/man1/slcs-init.1 $(DESTDIR)$(mandir)/man1
	install -d $(DESTDIR)$(docdir)
	install -m 0644 target/$(name)-package/share/doc/slcs/* $(DESTDIR)$(docdir)


#
# RPM
#
spec:
	@echo "Setting version and release in spec file: $(version)-$(release)"
	sed -e 's#@@SPEC_VERSION@@#$(version)#g' -e 's#@@SPEC_RELEASE@@#$(release)#g' $(spec_file).in > $(spec_file)


pre_rpmbuild: spec
	@echo "Preparing for rpmbuild in $(rpmbuild_dir)"
	mkdir -p $(rpmbuild_dir)/BUILD $(rpmbuild_dir)/RPMS $(rpmbuild_dir)/SOURCES $(rpmbuild_dir)/SPECS $(rpmbuild_dir)/SRPMS
	test -f $(name)-$(version).tar.gz || make dist
	cp $(name)-$(version).tar.gz $(rpmbuild_dir)/SOURCES


srpm: pre_rpmbuild
	@echo "Building SRPM in $(rpmbuild_dir)"
	rpmbuild --nodeps -v -bs $(spec_file) --define "_topdir $(rpmbuild_dir)"
	cp $(rpmbuild_dir)/SRPMS/*.src.rpm .


rpm: pre_rpmbuild
	@echo "Building RPM/SRPM in $(rpmbuild_dir)"
	rpmbuild --nodeps -v -ba $(spec_file) --define "_topdir $(rpmbuild_dir)"
	find $(rpmbuild_dir)/RPMS -name "*.rpm" -exec cp '{}' . \;

#
# Debian
#
pre_debbuild:
	@echo "Prepare for Debian building in $(debbuild_dir)"
	mkdir -p $(debbuild_dir)
	test -f $(name)-$(version).tar.gz || make dist
	cp $(name)-$(version).tar.gz $(debbuild_dir)/$(name)_$(version).orig.tar.gz
	tar -C $(debbuild_dir) -xzf $(debbuild_dir)/$(name)_$(version).orig.tar.gz

deb-src: pre_debbuild
	@echo "Building Debian source package in $(debbuild_dir)"
	cd $(debbuild_dir) && dpkg-source -b $(name)-$(version)
	find $(debbuild_dir) -maxdepth 1 -type f -exec cp '{}' . \;

deb: pre_debbuild
	@echo "Building Debian package in $(debbuild_dir)"
	cd $(debbuild_dir)/$(name)-$(version) && debuild -us -uc 
	find $(debbuild_dir) -maxdepth 1 -name "*.deb" -exec cp '{}' . \;

#
# OS X package
#
osx-pkg:
	@echo "Building OS X package in $(tmp_dir)"
	test ! -d $(tmp_dir) || rm -fr $(tmp_dir)
	make DESTDIR=$(tmp_dir) prefix=/usr sysconfdir=/etc install
	pkgbuild --identifier org.glite.slcs.client --version $(version)-$(release) --root $(tmp_dir) $(name)-$(version)-$(release).pkg
