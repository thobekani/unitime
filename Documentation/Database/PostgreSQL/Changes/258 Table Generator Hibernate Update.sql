/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/

alter table hibernate_unique_key add sequence_name varchar(100) not null default 'default';
alter table hibernate_unique_key add constraint pk_hibernate_unique_key primary key (sequence_name);
alter table instructional_offering alter instr_offering_perm_id type bigint;
		
/*
 * Update database version
 */
  
update application_config set value='258' where name='tmtbl.db.version';

commit;
