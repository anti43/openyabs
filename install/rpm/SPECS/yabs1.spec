Summary: YaBS Version 1
Name: yabs
Version: 1.0
Release: 3
License: GPL
Group: Applications/Office
BuildRoot: %{_builddir}/%{name}-root
URL: http://openyabs.org
Vendor: anti43
Packager: Andreas Weber
Prefix: %{install_dir}
BuildArchitectures: noarch
Requires:   java >= 1.6

%define my_dir /home/anti/NetBeansProjects/trunk
%define install_dir /usr/local
%define Version 1
%define Release 0

%description
YaBS Crossplatform Billing and Accounting Application

%prep


%build
cd %{my_dir}/dist/
rm -rf README.TXT
tar -cf %{_builddir}/yabs%{Version}.%{Release}.tar *

%install
pwd
rm -rf $RPM_BUILD_ROOT
rm -rf $RPM_BUILD_ROOT%{install_dir}/yabs/yabs%{Version}
mkdir -p $RPM_BUILD_ROOT%{install_dir}/yabs/yabs%{Version}
cd $RPM_BUILD_ROOT%{install_dir}/yabs/yabs%{Version}
tar -xf $RPM_BUILD_ROOT/../yabs%{Version}.%{Release}.tar

# Create menu entry
mkdir -p $RPM_BUILD_ROOT%{install_dir}/yabs/yabs%{Version}/misc
cat > $RPM_BUILD_ROOT%{install_dir}/yabs/yabs%{Version}/misc/yabs%{Version}.desktop <<EOF
[Desktop Entry]
Name=YaBS 1.0
Comment=Crossplatform Accounting and Billing Application
Exec=java -jar %{install_dir}/yabs/yabs%{Version}/yabs.jar
Icon=%{install_dir}/yabs/yabs%{Version}/misc/yabsd.png
Terminal=False
StartupNotify=false
Categories=Office;X-SuSE-Core-Office
Encoding=UTF-8
Type=Application
EOF


# Create run script
cat > $RPM_BUILD_ROOT%{install_dir}/yabs/yabs%{Version}/misc/yabs%{Version}.sh <<EOF
#!/bin/bash
java -jar %{install_dir}/yabs/yabs%{Version}/yabs.jar $1 $2 $3 $4 $5
EOF

chmod 644 $RPM_BUILD_ROOT%{install_dir}/yabs/yabs%{Version}/misc/yabs%{Version}.desktop
cp -f $RPM_BUILD_ROOT%{install_dir}/yabs/yabs%{Version}/misc/yabs%{Version}.desktop /usr/share/applications/yabs%{Version}.desktop
cp -f %{my_dir}/install/yabsd.png $RPM_BUILD_ROOT%{install_dir}/yabs/yabs%{Version}/misc/yabsd.png

%post
ln -fs %{install_dir}/yabs/yabs%{Version}/misc/yabs%{Version}.sh  /usr/bin/yabs

%preun
unlink /usr/bin/yabs

%postun
rm -f /usr/share/applications/yabs%{Version}.desktop

%clean
rm -rf $RPM_BUILD_ROOT
rm -rf %{_builddir}/yabs%{Version}.%{Release}.tar *

%files
%defattr(755,root,root)
%{install_dir}/yabs/yabs%{Version}/yabs.jar
%{install_dir}/yabs/yabs%{Version}/lib/*.jar
%{install_dir}/yabs/yabs%{Version}/misc/yabsd.png
%{install_dir}/yabs/yabs%{Version}/misc/yabs%{Version}.desktop

%attr(755,root,root)
%{install_dir}/yabs/yabs%{Version}/misc/yabs%{Version}.sh



%changelog
* Sun Sep 6 2009 Anti43
- Changed names from MP to YaBS
* Sun Jun 21 2009 Anti43
- Added Requires Java >= 1.6
- Added symlink creation of /usr/bin/yabs
* Sun May 03 2009 Anti43
- Created initial spec file
