delete from input_output;
delete from document;
delete from balance;
delete from product;
update warehouse.hibernate_sequence set next_val = 1;
commit;