##########################################################################
### template pour creer un RPM de script shell ###########################
##########################################################################
#
#  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#  Image du RPM crée
#  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#  os
#  ├── /etc
#  │   ├── /{{nom-du-projet}}
#  │   │   └── fichiers configs
#  │   ├── /rsyslog.d
#  │   │   └── fichiers configs
#  │   └── /logrotate.d
#  │       └── fichiers configs
#  └── /usr
#      └── /local
#          └── /gestexam
#              └── archives : jar/tgz
#  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#
##########################################################################


%define __jar_repack      %{nil}


## variables a fournir
## -- rpm_name          null
## -- rpm_version       0.1
## -- rpm_release       0



Summary:        %{rpm_name}
Name:           %{rpm_name}
Version:        %{rpm_version}
Release:        %{rpm_release}%{?dist}
BuildArch:	noarch
License:        Apache Software License
Group:          Scolarite
URL:            https://sources.dauphine.psl.eu/scolarite/gestexam
BuildRoot:	%{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildRequires:  redhat-lsb-core, tree, maven
Requires:	tomcat9.0, java-11-openjdk, java-11-openjdk-headless, jq, tee

%description
estage (voir spec : https://sources.dauphine.psl.eu/scolarite/estage).


## #prep
##echo "setup %{name}-%{version}.tar.gz (source0)"
##%setup -q


## #build
##mvn clean package


%install
# ~~ dossier de conf de la webapp
install -d -m 755          %{buildroot}/etc/%{name}
cp -r ./etc/%{name}/*      %{buildroot}/etc/%{name}/
# ~~ configuration des logs
##install -d -m 755          %{buildroot}/etc/rsyslog.d
##cp -r ./etc/rsyslog.d/*    %{buildroot}/etc/rsyslog.d/
# ~~ configuration des logs
##install -d -m 755          %{buildroot}/etc/logrotate.d
##cp -r ./etc/logrotate.d/*  %{buildroot}/etc/logrotate.d/

# ~~ scripts locaux (supervision)
install -d -m 755                %{buildroot}/usr/local/%{name}
# ++ copy du binaire precedement builder par "gitlab pipeline"
cp -r ./estage-*.war     %{buildroot}/usr/local/%{name}/estage.war
# ++ copy des scripts resources
cp    ./scripts/*                %{buildroot}/usr/local/%{name}/

# ~~ dossier web
#mkdir -p                         %{buildroot}/var/www/html
#cp ./webroot.tgz                 %{buildroot}/var/www/html/
#( cd %{buildroot}/var/www/html/; tar xfz webroot.tgz; mv build gestexam; rm -f webroot.tgz )

# ~~ logs output
#install -d -m 755          %{buildroot}/var/log/%{name}

# ~~ export-dir document
#install -d -m 755          %{buildroot}/opt/%{name}/upload-documents

# DEBUG
tree -L 4 %{buildroot}/

%files
%attr(-,root,root)                              /etc/%{name}
##%attr(-,root,root)                              /etc/rsyslog.d/*
##%attr(-,root,root)                              /etc/logrotate.d/*
%attr(-,root,root)                              /opt/%{name}/upload-documents
%attr(-,root,root)                              /usr/local/%{name}
%attr(-,root,root)                              /var/log/%{name}
%attr(-,root,root)                              /var/www/html/%{name}


%clean
echo "-- %{name} : clean buildroot - %{buildroot}"
rm -rf %{buildroot}
