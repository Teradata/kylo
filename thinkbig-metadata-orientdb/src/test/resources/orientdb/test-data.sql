
#
# Feed A
#
CREATE VERTEX DataSet SET id = UUID(), name = 'Dataset A';
CREATE VERTEX DataSet SET id = UUID(), name = 'Dataset AX';
CREATE VERTEX Feed SET id = UUID(), name = 'Feed A';
CREATE VERTEX DataSource SET id = UUID(), name = 'DataSource A';
CREATE VERTEX DataDestination SET id = UUID(), name = 'DataDestination AX';
CREATE EDGE Accesses FROM ( SELECT FROM DataSource WHERE name='DataSource A' ) TO ( SELECT FROM Dataset WHERE name='Dataset A' );
CREATE EDGE Accesses FROM ( SELECT FROM DataDestination WHERE name='DataDestination AX' ) TO ( SELECT FROM Dataset WHERE name='Dataset AX' );
CREATE EDGE ReadsFrom FROM ( SELECT FROM Feed WHERE name='Feed A' ) TO ( SELECT FROM DataSource WHERE name='DataSource A' );
CREATE EDGE WritesTo FROM ( SELECT FROM Feed WHERE name='Feed A' ) TO ( SELECT FROM DataDestination WHERE name='DataDestination AX' );

#
# Feed B
#
CREATE VERTEX DataSet SET id = UUID(), name = 'Dataset B';
CREATE VERTEX DataSet SET id = UUID(), name = 'Dataset BX';
CREATE VERTEX Feed SET id = UUID(), name = 'Feed B';
CREATE VERTEX DataSource SET id = UUID(), name = 'DataSource B';
CREATE VERTEX DataDestination SET id = UUID(), name = 'DataDestination BX';
CREATE EDGE Accesses FROM ( SELECT FROM DataSource WHERE name='DataSource B' ) TO ( SELECT FROM Dataset WHERE name='Dataset B' );
CREATE EDGE Accesses FROM ( SELECT FROM DataDestination WHERE name='DataDestination BX' ) TO ( SELECT FROM Dataset WHERE name='Dataset BX' );
CREATE EDGE ReadsFrom FROM ( SELECT FROM Feed WHERE name='Feed B' ) TO ( SELECT FROM DataSource WHERE name='DataSource B' );
CREATE EDGE WritesTo FROM ( SELECT FROM Feed WHERE name='Feed B' ) TO ( SELECT FROM DataDestination WHERE name='DataDestination BX' );

#
# Feed C
#
CREATE VERTEX DataSet SET id = UUID(), name = 'Dataset C';
CREATE VERTEX DataSet SET id = UUID(), name = 'Dataset CX';
CREATE VERTEX Feed SET id = UUID(), name = 'Feed C';
CREATE VERTEX DataSource SET id = UUID(), name = 'DataSource C';
CREATE VERTEX DataDestination SET id = UUID(), name = 'DataDestination CX';
CREATE EDGE Accesses FROM ( SELECT FROM DataSource WHERE name='DataSource C' ) TO ( SELECT FROM Dataset WHERE name='Dataset C' );
CREATE EDGE Accesses FROM ( SELECT FROM DataDestination WHERE name='DataDestination CX' ) TO ( SELECT FROM Dataset WHERE name='Dataset CX' );
CREATE EDGE ReadsFrom FROM ( SELECT FROM Feed WHERE name='Feed C' ) TO ( SELECT FROM DataSource WHERE name='DataSource C' );
CREATE EDGE WritesTo FROM ( SELECT FROM Feed WHERE name='Feed C' ) TO ( SELECT FROM DataDestination WHERE name='DataDestination CX' );

#
# Feed X
#
CREATE VERTEX DataSet SET id = UUID(), name = 'Dataset XZ';
CREATE VERTEX Feed SET id = UUID(), name = 'Feed X';
CREATE VERTEX DataSource SET id = UUID(), name = 'DataSource AX';
CREATE VERTEX DataSource SET id = UUID(), name = 'DataSource BX';
CREATE VERTEX DataSource SET id = UUID(), name = 'DataSource CX';
CREATE VERTEX DataDestination SET id = UUID(), name = 'DataDestination XZ';
CREATE EDGE Accesses FROM ( SELECT FROM DataSource WHERE name='DataSource AX' ) TO ( SELECT FROM Dataset WHERE name='Dataset AX' );
CREATE EDGE Accesses FROM ( SELECT FROM DataSource WHERE name='DataSource BX' ) TO ( SELECT FROM Dataset WHERE name='Dataset BX' );
CREATE EDGE Accesses FROM ( SELECT FROM DataSource WHERE name='DataSource CX' ) TO ( SELECT FROM Dataset WHERE name='Dataset CX' );
CREATE EDGE Accesses FROM ( SELECT FROM DataDestination WHERE name='DataDestination XZ' ) TO ( SELECT FROM Dataset WHERE name='Dataset XZ' );
CREATE EDGE ReadsFrom FROM ( SELECT FROM Feed WHERE name='Feed X' ) TO ( SELECT FROM DataSource WHERE name='DataSource AX' );
CREATE EDGE ReadsFrom FROM ( SELECT FROM Feed WHERE name='Feed X' ) TO ( SELECT FROM DataSource WHERE name='DataSource BX' );
CREATE EDGE ReadsFrom FROM ( SELECT FROM Feed WHERE name='Feed X' ) TO ( SELECT FROM DataSource WHERE name='DataSource CX' );
CREATE EDGE WritesTo FROM ( SELECT FROM Feed WHERE name='Feed X' ) TO ( SELECT FROM DataDestination WHERE name='DataDestination XZ' );

#
# Feed Y
#
CREATE VERTEX DataSet SET id = UUID(), name = 'Dataset Y';
CREATE VERTEX DataSet SET id = UUID(), name = 'Dataset YZ';
CREATE VERTEX Feed SET id = UUID(), name = 'Feed Y';
CREATE VERTEX DataSource SET id = UUID(), name = 'DataSource Y';
CREATE VERTEX DataDestination SET id = UUID(), name = 'DataDestination YZ';
CREATE EDGE Accesses FROM ( SELECT FROM DataSource WHERE name='DataSource Y' ) TO ( SELECT FROM Dataset WHERE name='Dataset Y' );
CREATE EDGE Accesses FROM ( SELECT FROM DataDestination WHERE name='DataDestination YZ' ) TO ( SELECT FROM Dataset WHERE name='Dataset YZ' );
CREATE EDGE ReadsFrom FROM ( SELECT FROM Feed WHERE name='Feed Y' ) TO ( SELECT FROM DataSource WHERE name='DataSource Y' );
CREATE EDGE WritesTo FROM ( SELECT FROM Feed WHERE name='Feed Y' ) TO ( SELECT FROM DataDestination WHERE name='DataDestination YZ' );

#
# Feed Z
#
CREATE VERTEX DataSet SET id = UUID(), name = 'Dataset Z';
CREATE VERTEX Feed SET id = UUID(), name = 'Feed Z';
CREATE VERTEX DataSource SET id = UUID(), name = 'DataSource XZ';
CREATE VERTEX DataSource SET id = UUID(), name = 'DataSource YZ';
CREATE VERTEX DataDestination SET id = UUID(), name = 'DataDestination Z';
CREATE EDGE Accesses FROM ( SELECT FROM DataSource WHERE name='DataSource XZ' ) TO ( SELECT FROM Dataset WHERE name='Dataset XZ' );
CREATE EDGE Accesses FROM ( SELECT FROM DataSource WHERE name='DataSource YZ' ) TO ( SELECT FROM Dataset WHERE name='Dataset YZ' );
CREATE EDGE Accesses FROM ( SELECT FROM DataDestination WHERE name='DataDestination Z' ) TO ( SELECT FROM Dataset WHERE name='Dataset Z' );
CREATE EDGE ReadsFrom FROM ( SELECT FROM Feed WHERE name='Feed Z' ) TO ( SELECT FROM DataSource WHERE name='DataSource XZ' );
CREATE EDGE ReadsFrom FROM ( SELECT FROM Feed WHERE name='Feed Z' ) TO ( SELECT FROM DataSource WHERE name='DataSource YZ' );
CREATE EDGE WritesTo FROM ( SELECT FROM Feed WHERE name='Feed Z' ) TO ( SELECT FROM DataDestination WHERE name='DataDestination Z' );


