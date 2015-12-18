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
name|mapper
operator|.
name|core
operator|.
name|BinaryFieldMapper
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
name|core
operator|.
name|BooleanFieldMapper
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
name|core
operator|.
name|ByteFieldMapper
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
name|core
operator|.
name|CompletionFieldMapper
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
name|core
operator|.
name|DateFieldMapper
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
name|core
operator|.
name|DoubleFieldMapper
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
name|core
operator|.
name|FloatFieldMapper
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
name|core
operator|.
name|IntegerFieldMapper
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
name|core
operator|.
name|LongFieldMapper
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
name|core
operator|.
name|ShortFieldMapper
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
name|core
operator|.
name|StringFieldMapper
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
name|core
operator|.
name|TokenCountFieldMapper
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
name|geo
operator|.
name|GeoShapeFieldMapper
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
name|ip
operator|.
name|IpFieldMapper
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
name|object
operator|.
name|ObjectMapper
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
name|object
operator|.
name|RootObjectMapper
import|;
end_import

begin_class
DECL|class|MapperBuilders
specifier|public
specifier|final
class|class
name|MapperBuilders
block|{
DECL|method|MapperBuilders
specifier|private
name|MapperBuilders
parameter_list|()
block|{}
DECL|method|doc
specifier|public
specifier|static
name|DocumentMapper
operator|.
name|Builder
name|doc
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|RootObjectMapper
operator|.
name|Builder
name|objectBuilder
parameter_list|,
name|MapperService
name|mapperService
parameter_list|)
block|{
return|return
operator|new
name|DocumentMapper
operator|.
name|Builder
argument_list|(
name|settings
argument_list|,
name|objectBuilder
argument_list|,
name|mapperService
argument_list|)
return|;
block|}
DECL|method|rootObject
specifier|public
specifier|static
name|RootObjectMapper
operator|.
name|Builder
name|rootObject
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|RootObjectMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|object
specifier|public
specifier|static
name|ObjectMapper
operator|.
name|Builder
name|object
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|ObjectMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|booleanField
specifier|public
specifier|static
name|BooleanFieldMapper
operator|.
name|Builder
name|booleanField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|BooleanFieldMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|stringField
specifier|public
specifier|static
name|StringFieldMapper
operator|.
name|Builder
name|stringField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|StringFieldMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|binaryField
specifier|public
specifier|static
name|BinaryFieldMapper
operator|.
name|Builder
name|binaryField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|BinaryFieldMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|dateField
specifier|public
specifier|static
name|DateFieldMapper
operator|.
name|Builder
name|dateField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|DateFieldMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|ipField
specifier|public
specifier|static
name|IpFieldMapper
operator|.
name|Builder
name|ipField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|IpFieldMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|shortField
specifier|public
specifier|static
name|ShortFieldMapper
operator|.
name|Builder
name|shortField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|ShortFieldMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|byteField
specifier|public
specifier|static
name|ByteFieldMapper
operator|.
name|Builder
name|byteField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|ByteFieldMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|integerField
specifier|public
specifier|static
name|IntegerFieldMapper
operator|.
name|Builder
name|integerField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|IntegerFieldMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|tokenCountField
specifier|public
specifier|static
name|TokenCountFieldMapper
operator|.
name|Builder
name|tokenCountField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|TokenCountFieldMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|longField
specifier|public
specifier|static
name|LongFieldMapper
operator|.
name|Builder
name|longField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|LongFieldMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|floatField
specifier|public
specifier|static
name|FloatFieldMapper
operator|.
name|Builder
name|floatField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|FloatFieldMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|doubleField
specifier|public
specifier|static
name|DoubleFieldMapper
operator|.
name|Builder
name|doubleField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|DoubleFieldMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|geoShapeField
specifier|public
specifier|static
name|GeoShapeFieldMapper
operator|.
name|Builder
name|geoShapeField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|GeoShapeFieldMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|completionField
specifier|public
specifier|static
name|CompletionFieldMapper
operator|.
name|Builder
name|completionField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|CompletionFieldMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
return|;
block|}
block|}
end_class

end_unit

