begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.internal
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|internal
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|DocValuesType
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

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|FieldDataType
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
name|ContentPath
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
operator|.
name|Loading
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
name|Mapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESTestCase
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
operator|.
name|settingsBuilder
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|is
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|nullValue
import|;
end_import

begin_class
DECL|class|ParentFieldMapperTests
specifier|public
class|class
name|ParentFieldMapperTests
extends|extends
name|ESTestCase
block|{
DECL|method|testPost2Dot0LazyLoading
specifier|public
name|void
name|testPost2Dot0LazyLoading
parameter_list|()
block|{
name|ParentFieldMapper
operator|.
name|Builder
name|builder
init|=
operator|new
name|ParentFieldMapper
operator|.
name|Builder
argument_list|(
literal|"child"
argument_list|)
decl_stmt|;
name|builder
operator|.
name|type
argument_list|(
literal|"parent"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|fieldDataSettings
argument_list|(
name|createFDSettings
argument_list|(
name|Loading
operator|.
name|LAZY
argument_list|)
argument_list|)
expr_stmt|;
name|ParentFieldMapper
name|parentFieldMapper
init|=
name|builder
operator|.
name|build
argument_list|(
operator|new
name|Mapper
operator|.
name|BuilderContext
argument_list|(
name|post2Dot0IndexSettings
argument_list|()
argument_list|,
operator|new
name|ContentPath
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getParentJoinFieldType
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_parent#child"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getParentJoinFieldType
argument_list|()
operator|.
name|fieldDataType
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getParentJoinFieldType
argument_list|()
operator|.
name|hasDocValues
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getParentJoinFieldType
argument_list|()
operator|.
name|docValuesType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|DocValuesType
operator|.
name|SORTED
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getChildJoinFieldType
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_parent#parent"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getChildJoinFieldType
argument_list|()
operator|.
name|fieldDataType
argument_list|()
operator|.
name|getLoading
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Loading
operator|.
name|LAZY
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getChildJoinFieldType
argument_list|()
operator|.
name|hasDocValues
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getChildJoinFieldType
argument_list|()
operator|.
name|docValuesType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|DocValuesType
operator|.
name|SORTED
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testPost2Dot0EagerLoading
specifier|public
name|void
name|testPost2Dot0EagerLoading
parameter_list|()
block|{
name|ParentFieldMapper
operator|.
name|Builder
name|builder
init|=
operator|new
name|ParentFieldMapper
operator|.
name|Builder
argument_list|(
literal|"child"
argument_list|)
decl_stmt|;
name|builder
operator|.
name|type
argument_list|(
literal|"parent"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|fieldDataSettings
argument_list|(
name|createFDSettings
argument_list|(
name|Loading
operator|.
name|EAGER
argument_list|)
argument_list|)
expr_stmt|;
name|ParentFieldMapper
name|parentFieldMapper
init|=
name|builder
operator|.
name|build
argument_list|(
operator|new
name|Mapper
operator|.
name|BuilderContext
argument_list|(
name|post2Dot0IndexSettings
argument_list|()
argument_list|,
operator|new
name|ContentPath
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getParentJoinFieldType
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_parent#child"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getParentJoinFieldType
argument_list|()
operator|.
name|fieldDataType
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getParentJoinFieldType
argument_list|()
operator|.
name|hasDocValues
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getParentJoinFieldType
argument_list|()
operator|.
name|docValuesType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|DocValuesType
operator|.
name|SORTED
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getChildJoinFieldType
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_parent#parent"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getChildJoinFieldType
argument_list|()
operator|.
name|fieldDataType
argument_list|()
operator|.
name|getLoading
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Loading
operator|.
name|EAGER
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getChildJoinFieldType
argument_list|()
operator|.
name|hasDocValues
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getChildJoinFieldType
argument_list|()
operator|.
name|docValuesType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|DocValuesType
operator|.
name|SORTED
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testPost2Dot0EagerGlobalOrdinalsLoading
specifier|public
name|void
name|testPost2Dot0EagerGlobalOrdinalsLoading
parameter_list|()
block|{
name|ParentFieldMapper
operator|.
name|Builder
name|builder
init|=
operator|new
name|ParentFieldMapper
operator|.
name|Builder
argument_list|(
literal|"child"
argument_list|)
decl_stmt|;
name|builder
operator|.
name|type
argument_list|(
literal|"parent"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|fieldDataSettings
argument_list|(
name|createFDSettings
argument_list|(
name|Loading
operator|.
name|EAGER_GLOBAL_ORDINALS
argument_list|)
argument_list|)
expr_stmt|;
name|ParentFieldMapper
name|parentFieldMapper
init|=
name|builder
operator|.
name|build
argument_list|(
operator|new
name|Mapper
operator|.
name|BuilderContext
argument_list|(
name|post2Dot0IndexSettings
argument_list|()
argument_list|,
operator|new
name|ContentPath
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getParentJoinFieldType
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_parent#child"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getParentJoinFieldType
argument_list|()
operator|.
name|fieldDataType
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getParentJoinFieldType
argument_list|()
operator|.
name|hasDocValues
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getParentJoinFieldType
argument_list|()
operator|.
name|docValuesType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|DocValuesType
operator|.
name|SORTED
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getChildJoinFieldType
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_parent#parent"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getChildJoinFieldType
argument_list|()
operator|.
name|fieldDataType
argument_list|()
operator|.
name|getLoading
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Loading
operator|.
name|EAGER_GLOBAL_ORDINALS
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getChildJoinFieldType
argument_list|()
operator|.
name|hasDocValues
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getChildJoinFieldType
argument_list|()
operator|.
name|docValuesType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|DocValuesType
operator|.
name|SORTED
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testPre2Dot0LazyLoading
specifier|public
name|void
name|testPre2Dot0LazyLoading
parameter_list|()
block|{
name|ParentFieldMapper
operator|.
name|Builder
name|builder
init|=
operator|new
name|ParentFieldMapper
operator|.
name|Builder
argument_list|(
literal|"child"
argument_list|)
decl_stmt|;
name|builder
operator|.
name|type
argument_list|(
literal|"parent"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|fieldDataSettings
argument_list|(
name|createFDSettings
argument_list|(
name|Loading
operator|.
name|LAZY
argument_list|)
argument_list|)
expr_stmt|;
name|ParentFieldMapper
name|parentFieldMapper
init|=
name|builder
operator|.
name|build
argument_list|(
operator|new
name|Mapper
operator|.
name|BuilderContext
argument_list|(
name|pre2Dot0IndexSettings
argument_list|()
argument_list|,
operator|new
name|ContentPath
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getParentJoinFieldType
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_parent#child"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getParentJoinFieldType
argument_list|()
operator|.
name|fieldDataType
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getParentJoinFieldType
argument_list|()
operator|.
name|hasDocValues
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getParentJoinFieldType
argument_list|()
operator|.
name|docValuesType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|DocValuesType
operator|.
name|NONE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getChildJoinFieldType
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_parent#parent"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getChildJoinFieldType
argument_list|()
operator|.
name|fieldDataType
argument_list|()
operator|.
name|getLoading
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Loading
operator|.
name|LAZY
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getChildJoinFieldType
argument_list|()
operator|.
name|hasDocValues
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getChildJoinFieldType
argument_list|()
operator|.
name|docValuesType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|DocValuesType
operator|.
name|NONE
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testPre2Dot0EagerLoading
specifier|public
name|void
name|testPre2Dot0EagerLoading
parameter_list|()
block|{
name|ParentFieldMapper
operator|.
name|Builder
name|builder
init|=
operator|new
name|ParentFieldMapper
operator|.
name|Builder
argument_list|(
literal|"child"
argument_list|)
decl_stmt|;
name|builder
operator|.
name|type
argument_list|(
literal|"parent"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|fieldDataSettings
argument_list|(
name|createFDSettings
argument_list|(
name|Loading
operator|.
name|EAGER
argument_list|)
argument_list|)
expr_stmt|;
name|ParentFieldMapper
name|parentFieldMapper
init|=
name|builder
operator|.
name|build
argument_list|(
operator|new
name|Mapper
operator|.
name|BuilderContext
argument_list|(
name|pre2Dot0IndexSettings
argument_list|()
argument_list|,
operator|new
name|ContentPath
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getParentJoinFieldType
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_parent#child"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getParentJoinFieldType
argument_list|()
operator|.
name|fieldDataType
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getParentJoinFieldType
argument_list|()
operator|.
name|hasDocValues
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getParentJoinFieldType
argument_list|()
operator|.
name|docValuesType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|DocValuesType
operator|.
name|NONE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getChildJoinFieldType
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_parent#parent"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getChildJoinFieldType
argument_list|()
operator|.
name|fieldDataType
argument_list|()
operator|.
name|getLoading
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Loading
operator|.
name|EAGER
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getChildJoinFieldType
argument_list|()
operator|.
name|hasDocValues
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getChildJoinFieldType
argument_list|()
operator|.
name|docValuesType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|DocValuesType
operator|.
name|NONE
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testPre2Dot0EagerGlobalOrdinalsLoading
specifier|public
name|void
name|testPre2Dot0EagerGlobalOrdinalsLoading
parameter_list|()
block|{
name|ParentFieldMapper
operator|.
name|Builder
name|builder
init|=
operator|new
name|ParentFieldMapper
operator|.
name|Builder
argument_list|(
literal|"child"
argument_list|)
decl_stmt|;
name|builder
operator|.
name|type
argument_list|(
literal|"parent"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|fieldDataSettings
argument_list|(
name|createFDSettings
argument_list|(
name|Loading
operator|.
name|EAGER_GLOBAL_ORDINALS
argument_list|)
argument_list|)
expr_stmt|;
name|ParentFieldMapper
name|parentFieldMapper
init|=
name|builder
operator|.
name|build
argument_list|(
operator|new
name|Mapper
operator|.
name|BuilderContext
argument_list|(
name|pre2Dot0IndexSettings
argument_list|()
argument_list|,
operator|new
name|ContentPath
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getParentJoinFieldType
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_parent#child"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getParentJoinFieldType
argument_list|()
operator|.
name|fieldDataType
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getParentJoinFieldType
argument_list|()
operator|.
name|hasDocValues
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getParentJoinFieldType
argument_list|()
operator|.
name|docValuesType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|DocValuesType
operator|.
name|NONE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getChildJoinFieldType
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_parent#parent"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getChildJoinFieldType
argument_list|()
operator|.
name|fieldDataType
argument_list|()
operator|.
name|getLoading
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Loading
operator|.
name|EAGER_GLOBAL_ORDINALS
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getChildJoinFieldType
argument_list|()
operator|.
name|hasDocValues
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parentFieldMapper
operator|.
name|getChildJoinFieldType
argument_list|()
operator|.
name|docValuesType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|DocValuesType
operator|.
name|NONE
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|pre2Dot0IndexSettings
specifier|private
specifier|static
name|Settings
name|pre2Dot0IndexSettings
parameter_list|()
block|{
return|return
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
name|V_1_6_3
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|post2Dot0IndexSettings
specifier|private
specifier|static
name|Settings
name|post2Dot0IndexSettings
parameter_list|()
block|{
return|return
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
name|V_2_1_0
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|createFDSettings
specifier|private
specifier|static
name|Settings
name|createFDSettings
parameter_list|(
name|Loading
name|loading
parameter_list|)
block|{
return|return
operator|new
name|FieldDataType
argument_list|(
literal|"child"
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Loading
operator|.
name|KEY
argument_list|,
name|loading
argument_list|)
argument_list|)
operator|.
name|getSettings
argument_list|()
return|;
block|}
block|}
end_class

end_unit

