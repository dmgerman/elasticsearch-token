begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_class
DECL|class|UUIDs
specifier|public
class|class
name|UUIDs
block|{
DECL|field|RANDOM_UUID_GENERATOR
specifier|private
specifier|static
specifier|final
name|RandomBasedUUIDGenerator
name|RANDOM_UUID_GENERATOR
init|=
operator|new
name|RandomBasedUUIDGenerator
argument_list|()
decl_stmt|;
DECL|field|TIME_UUID_GENERATOR
specifier|private
specifier|static
specifier|final
name|UUIDGenerator
name|TIME_UUID_GENERATOR
init|=
operator|new
name|TimeBasedUUIDGenerator
argument_list|()
decl_stmt|;
comment|/** Generates a time-based UUID (similar to Flake IDs), which is preferred when generating an ID to be indexed into a Lucene index as      *  primary key.  The id is opaque and the implementation is free to change at any time! */
DECL|method|base64UUID
specifier|public
specifier|static
name|String
name|base64UUID
parameter_list|()
block|{
return|return
name|TIME_UUID_GENERATOR
operator|.
name|getBase64UUID
argument_list|()
return|;
block|}
comment|/** Returns a Base64 encoded version of a Version 4.0 compatible UUID as defined here: http://www.ietf.org/rfc/rfc4122.txt, using the      *  provided {@code Random} instance */
DECL|method|randomBase64UUID
specifier|public
specifier|static
name|String
name|randomBase64UUID
parameter_list|(
name|Random
name|random
parameter_list|)
block|{
return|return
name|RANDOM_UUID_GENERATOR
operator|.
name|getBase64UUID
argument_list|(
name|random
argument_list|)
return|;
block|}
comment|/** Returns a Base64 encoded version of a Version 4.0 compatible UUID as defined here: http://www.ietf.org/rfc/rfc4122.txt, using a      *  private {@code SecureRandom} instance */
DECL|method|randomBase64UUID
specifier|public
specifier|static
name|String
name|randomBase64UUID
parameter_list|()
block|{
return|return
name|RANDOM_UUID_GENERATOR
operator|.
name|getBase64UUID
argument_list|()
return|;
block|}
block|}
end_class

end_unit
