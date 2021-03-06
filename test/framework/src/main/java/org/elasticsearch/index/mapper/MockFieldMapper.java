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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_comment
comment|// this sucks how much must be overridden just do get a dummy field mapper...
end_comment

begin_class
DECL|class|MockFieldMapper
specifier|public
class|class
name|MockFieldMapper
extends|extends
name|FieldMapper
block|{
DECL|field|dummySettings
specifier|static
name|Settings
name|dummySettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|CURRENT
operator|.
name|id
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
DECL|method|MockFieldMapper
specifier|public
name|MockFieldMapper
parameter_list|(
name|String
name|fullName
parameter_list|)
block|{
name|this
argument_list|(
name|fullName
argument_list|,
operator|new
name|FakeFieldType
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|MockFieldMapper
specifier|public
name|MockFieldMapper
parameter_list|(
name|String
name|fullName
parameter_list|,
name|MappedFieldType
name|fieldType
parameter_list|)
block|{
name|super
argument_list|(
name|findSimpleName
argument_list|(
name|fullName
argument_list|)
argument_list|,
name|setName
argument_list|(
name|fullName
argument_list|,
name|fieldType
argument_list|)
argument_list|,
name|setName
argument_list|(
name|fullName
argument_list|,
name|fieldType
argument_list|)
argument_list|,
name|dummySettings
argument_list|,
name|MultiFields
operator|.
name|empty
argument_list|()
argument_list|,
operator|new
name|CopyTo
operator|.
name|Builder
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|setName
specifier|static
name|MappedFieldType
name|setName
parameter_list|(
name|String
name|fullName
parameter_list|,
name|MappedFieldType
name|fieldType
parameter_list|)
block|{
name|fieldType
operator|.
name|setName
argument_list|(
name|fullName
argument_list|)
expr_stmt|;
return|return
name|fieldType
return|;
block|}
DECL|method|findSimpleName
specifier|static
name|String
name|findSimpleName
parameter_list|(
name|String
name|fullName
parameter_list|)
block|{
name|int
name|ndx
init|=
name|fullName
operator|.
name|lastIndexOf
argument_list|(
literal|'.'
argument_list|)
decl_stmt|;
return|return
name|fullName
operator|.
name|substring
argument_list|(
name|ndx
operator|+
literal|1
argument_list|)
return|;
block|}
DECL|class|FakeFieldType
specifier|public
specifier|static
class|class
name|FakeFieldType
extends|extends
name|TermBasedFieldType
block|{
DECL|method|FakeFieldType
specifier|public
name|FakeFieldType
parameter_list|()
block|{         }
DECL|method|FakeFieldType
specifier|protected
name|FakeFieldType
parameter_list|(
name|FakeFieldType
name|ref
parameter_list|)
block|{
name|super
argument_list|(
name|ref
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|clone
specifier|public
name|MappedFieldType
name|clone
parameter_list|()
block|{
return|return
operator|new
name|FakeFieldType
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|typeName
specifier|public
name|String
name|typeName
parameter_list|()
block|{
return|return
literal|"faketype"
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|contentType
specifier|protected
name|String
name|contentType
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|parseCreateField
specifier|protected
name|void
name|parseCreateField
parameter_list|(
name|ParseContext
name|context
parameter_list|,
name|List
name|list
parameter_list|)
throws|throws
name|IOException
block|{     }
block|}
end_class

end_unit

