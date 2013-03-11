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
release=2

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

.PHONY: clean dist package install

all: package

clean:
	rm -rf target $(tmp_dir) *.tar.gz

dist:
	@echo "Package the sources..."
	test ! -d $(tmp_dir) || rm -fr $(tmp_dir)
	mkdir -p $(tmp_dir)/$(name)-$(version)
	cp .classpath .project Makefile README.md pom.xml $(tmp_dir)/$(name)-$(version)
	cp -r doc $(tmp_dir)/$(name)-$(version)
	cp -r src $(tmp_dir)/$(name)-$(version)
	test ! -f $(name)-$(version).src.tar.gz || rm $(name)-$(version).src.tar.gz
	tar -C $(tmp_dir) -czf $(name)-$(version).src.tar.gz $(name)-$(version)
	rm -fr $(tmp_dir)

package:
	@echo "Build package with maven"
	mvn -B package

bin-package:
	test -d target/$(name)-package.dir || make package
	@echo "Package the binary..."
	test ! -f $(name)-$(version)-$(release).tar.gz || rm $(name)-$(version)-$(release).tar.gz
	tar -C target/$(name)-package.dir -czf $(name)-$(version)-$(release).tar.gz bin etc share

install:
	test -d target/$(name)-package.dir || make package
	@echo "Install binary in $(DESTDIR)$(prefix)"
	@echo " sysconfdir: $(DESTDIR)$(sysconfdir)"
	@echo " bindir: $(DESTDIR)$(bindir)"
	@echo " datadir: $(DESTDIR)$(datadir)"
	@echo " docdir: $(DESTDIR)$(docdir)"
	install -d $(DESTDIR)$(sysconfdir)/slcs
	install -m 0644 target/$(name)-package.dir/etc/slcs/* $(DESTDIR)$(sysconfdir)/slcs
	install -d $(DESTDIR)$(bindir)
	install -m 0755 target/$(name)-package.dir/bin/slcs-* $(DESTDIR)$(bindir)
	install -d $(DESTDIR)$(datadir)
	install -m 0644 target/$(name)-package.dir/share/slcs/*.jar $(DESTDIR)$(datadir)
	install -d $(DESTDIR)$(mandir)/man1
	install -m 0644 target/$(name)-package.dir/share/man/man1/slcs-init.1 $(DESTDIR)$(mandir)/man1
	install -d $(DESTDIR)$(docdir)
	install -m 0644 target/$(name)-package.dir/share/doc/slcs/* $(DESTDIR)$(docdir)

