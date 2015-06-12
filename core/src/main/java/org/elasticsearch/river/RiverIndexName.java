begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.river
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|river
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|BindingAnnotation
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
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Documented
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Retention
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Target
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|ElementType
operator|.
name|FIELD
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|ElementType
operator|.
name|PARAMETER
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|RetentionPolicy
operator|.
name|RUNTIME
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_annotation_defn
annotation|@
name|BindingAnnotation
annotation|@
name|Target
argument_list|(
block|{
name|FIELD
block|,
name|PARAMETER
block|}
argument_list|)
annotation|@
name|Retention
argument_list|(
name|RUNTIME
argument_list|)
annotation|@
name|Documented
DECL|interface|RiverIndexName
specifier|public
annotation_defn|@interface
name|RiverIndexName
block|{
DECL|class|Conf
specifier|static
class|class
name|Conf
block|{
DECL|field|DEFAULT_INDEX_NAME
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_INDEX_NAME
init|=
literal|"_river"
decl_stmt|;
DECL|method|indexName
specifier|public
specifier|static
name|String
name|indexName
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
return|return
name|settings
operator|.
name|get
argument_list|(
literal|"river.index_name"
argument_list|,
name|DEFAULT_INDEX_NAME
argument_list|)
return|;
block|}
block|}
block|}
end_annotation_defn

end_unit
