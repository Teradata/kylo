

select expand(out('ReadsFrom').out('Accesses').in('Accesses').in('WritesTo')) from Feed where name = 'Feed X'


select id, name from (select expand(out('ReadsFrom')) from Feed where name = 'Feed X')

select id, name from (select expand(out('ReadsFrom').out('Accesses').in('Accesses').in('WritesTo')) from Feed where name = 'Feed X')

select out('ReadsFrom') from Feed where name = 'Feed X'


select out('ReadsFrom').out('Accesses').in('Accesses').in('WritesTo') from Feed where name = 'Feed X';

select expand(out('ReadsFrom').out('Accesses').in('Accesses').in('WritesTo')) from Feed where name = 'Feed X';


select from ReadsFrom;
select from WritesTo;
select from Accesses;

select from Changes;
select from ResultsIn;
select from Produced;











