begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|LegacyShortFieldMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|MappedFieldType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_class
DECL|class|LegacyShortFieldTypeTests
specifier|public
class|class
name|LegacyShortFieldTypeTests
extends|extends
name|FieldTypeTestCase
block|{
annotation|@
name|Override
DECL|method|createDefaultFieldType
specifier|protected
name|MappedFieldType
name|createDefaultFieldType
parameter_list|()
block|{
return|return
operator|new
name|LegacyShortFieldMapper
operator|.
name|ShortFieldType
argument_list|()
return|;
block|}
annotation|@
name|Before
DECL|method|setupProperties
specifier|public
name|void
name|setupProperties
parameter_list|()
block|{
name|setDummyNullValue
argument_list|(
operator|(
name|short
operator|)
literal|10
argument_list|)
expr_stmt|;
block|}
DECL|method|testValueForSearch
specifier|public
name|void
name|testValueForSearch
parameter_list|()
block|{
name|MappedFieldType
name|ft
init|=
name|createDefaultFieldType
argument_list|()
decl_stmt|;
comment|// shorts are stored as ints
name|assertEquals
argument_list|(
name|Short
operator|.
name|valueOf
argument_list|(
operator|(
name|short
operator|)
literal|3
argument_list|)
argument_list|,
name|ft
operator|.
name|valueForSearch
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
literal|3
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
