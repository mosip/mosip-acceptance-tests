## Parser module
Parses the provided sheets and converts DSLs to classes

### PersonaSheet
Contains Persona related data

_*Column names are referred as "field cells"_

_*Column values are reffered as "value cells"_

* Field cells that are without {} are mandatory (system reserved)
* Field cells within {} will be treated as id object fields
* Field cells containing "^" will be treated as mutation-required field, random string will added to avoid duplication
* Value cells for multi-language field cells needs two values in respective languages (separated by %%)
* Value cells containing separator "%%" will be treated as multi-language value field, first group will be treat as primary language value and second group as secondary language value

**Examples -**

**Field cell: {fullName}**

id object field

**Field cell: {fullName}, value cell: Jane Doe%%jyn du**

id object field, multi-language field

**Field cell: ^{email}, value cell: abc@xyz.com**

id object field, mutation-required field (value will be converted to abc????@xyz.com)