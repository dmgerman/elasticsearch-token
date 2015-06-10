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
name|lucene
operator|.
name|Lucene
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
name|similarity
operator|.
name|BM25SimilarityProvider
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
name|ElasticsearchTestCase
import|;
end_import

begin_comment
comment|/** Base test case for subclasses of MappedFieldType */
end_comment

begin_class
DECL|class|FieldTypeTestCase
specifier|public
specifier|abstract
class|class
name|FieldTypeTestCase
extends|extends
name|ElasticsearchTestCase
block|{
comment|/** Create a default constructed fieldtype */
DECL|method|createDefaultFieldType
specifier|protected
specifier|abstract
name|MappedFieldType
name|createDefaultFieldType
parameter_list|()
function_decl|;
comment|/** A dummy null value to use when modifying null value */
DECL|method|dummyNullValue
specifier|protected
name|Object
name|dummyNullValue
parameter_list|()
block|{
return|return
literal|"dummyvalue"
return|;
block|}
comment|/** Returns the number of properties that can be modified for the fieldtype */
DECL|method|numProperties
specifier|protected
name|int
name|numProperties
parameter_list|()
block|{
return|return
literal|10
return|;
block|}
comment|/** Modifies a property, identified by propNum, on the given fieldtype */
DECL|method|modifyProperty
specifier|protected
name|void
name|modifyProperty
parameter_list|(
name|MappedFieldType
name|ft
parameter_list|,
name|int
name|propNum
parameter_list|)
block|{
switch|switch
condition|(
name|propNum
condition|)
block|{
case|case
literal|0
case|:
name|ft
operator|.
name|setNames
argument_list|(
operator|new
name|MappedFieldType
operator|.
name|Names
argument_list|(
literal|"dummy"
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|ft
operator|.
name|setBoost
argument_list|(
literal|1.1f
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|ft
operator|.
name|setHasDocValues
argument_list|(
operator|!
name|ft
operator|.
name|hasDocValues
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|3
case|:
name|ft
operator|.
name|setIndexAnalyzer
argument_list|(
name|Lucene
operator|.
name|STANDARD_ANALYZER
argument_list|)
expr_stmt|;
break|break;
case|case
literal|4
case|:
name|ft
operator|.
name|setSearchAnalyzer
argument_list|(
name|Lucene
operator|.
name|STANDARD_ANALYZER
argument_list|)
expr_stmt|;
break|break;
case|case
literal|5
case|:
name|ft
operator|.
name|setSearchQuoteAnalyzer
argument_list|(
name|Lucene
operator|.
name|STANDARD_ANALYZER
argument_list|)
expr_stmt|;
break|break;
case|case
literal|6
case|:
name|ft
operator|.
name|setSimilarity
argument_list|(
operator|new
name|BM25SimilarityProvider
argument_list|(
literal|"foo"
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|7
case|:
name|ft
operator|.
name|setNormsLoading
argument_list|(
name|MappedFieldType
operator|.
name|Loading
operator|.
name|LAZY
argument_list|)
expr_stmt|;
break|break;
case|case
literal|8
case|:
name|ft
operator|.
name|setFieldDataType
argument_list|(
operator|new
name|FieldDataType
argument_list|(
literal|"foo"
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"loading"
argument_list|,
literal|"eager"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|9
case|:
name|ft
operator|.
name|setNullValue
argument_list|(
name|dummyNullValue
argument_list|()
argument_list|)
expr_stmt|;
break|break;
default|default:
name|fail
argument_list|(
literal|"unknown fieldtype property number "
operator|+
name|propNum
argument_list|)
expr_stmt|;
block|}
block|}
comment|// TODO: remove this once toString is no longer final on FieldType...
DECL|method|assertEquals
specifier|protected
name|void
name|assertEquals
parameter_list|(
name|int
name|i
parameter_list|,
name|MappedFieldType
name|ft1
parameter_list|,
name|MappedFieldType
name|ft2
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"prop "
operator|+
name|i
operator|+
literal|"\nexpected: "
operator|+
name|toString
argument_list|(
name|ft1
argument_list|)
operator|+
literal|"; \nactual:   "
operator|+
name|toString
argument_list|(
name|ft2
argument_list|)
argument_list|,
name|ft1
argument_list|,
name|ft2
argument_list|)
expr_stmt|;
block|}
DECL|method|toString
specifier|protected
name|String
name|toString
parameter_list|(
name|MappedFieldType
name|ft
parameter_list|)
block|{
return|return
literal|"MappedFieldType{"
operator|+
literal|"names="
operator|+
name|ft
operator|.
name|names
argument_list|()
operator|+
literal|", boost="
operator|+
name|ft
operator|.
name|boost
argument_list|()
operator|+
literal|", docValues="
operator|+
name|ft
operator|.
name|hasDocValues
argument_list|()
operator|+
literal|", indexAnalyzer="
operator|+
name|ft
operator|.
name|indexAnalyzer
argument_list|()
operator|+
literal|", searchAnalyzer="
operator|+
name|ft
operator|.
name|searchAnalyzer
argument_list|()
operator|+
literal|", searchQuoteAnalyzer="
operator|+
name|ft
operator|.
name|searchQuoteAnalyzer
argument_list|()
operator|+
literal|", similarity="
operator|+
name|ft
operator|.
name|similarity
argument_list|()
operator|+
literal|", normsLoading="
operator|+
name|ft
operator|.
name|normsLoading
argument_list|()
operator|+
literal|", fieldDataType="
operator|+
name|ft
operator|.
name|fieldDataType
argument_list|()
operator|+
literal|", nullValue="
operator|+
name|ft
operator|.
name|nullValue
argument_list|()
operator|+
literal|", nullValueAsString='"
operator|+
name|ft
operator|.
name|nullValueAsString
argument_list|()
operator|+
literal|"'"
operator|+
literal|"} "
operator|+
name|super
operator|.
name|toString
argument_list|()
return|;
block|}
DECL|method|testClone
specifier|public
name|void
name|testClone
parameter_list|()
block|{
name|MappedFieldType
name|fieldType
init|=
name|createDefaultFieldType
argument_list|()
decl_stmt|;
name|MappedFieldType
name|clone
init|=
name|fieldType
operator|.
name|clone
argument_list|()
decl_stmt|;
name|assertNotSame
argument_list|(
name|clone
argument_list|,
name|fieldType
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|clone
operator|.
name|getClass
argument_list|()
argument_list|,
name|fieldType
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|clone
argument_list|,
name|fieldType
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|clone
argument_list|,
name|clone
operator|.
name|clone
argument_list|()
argument_list|)
expr_stmt|;
comment|// transitivity
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numProperties
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|fieldType
operator|=
name|createDefaultFieldType
argument_list|()
expr_stmt|;
name|modifyProperty
argument_list|(
name|fieldType
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|clone
operator|=
name|fieldType
operator|.
name|clone
argument_list|()
expr_stmt|;
name|assertNotSame
argument_list|(
name|clone
argument_list|,
name|fieldType
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|i
argument_list|,
name|clone
argument_list|,
name|fieldType
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testEquals
specifier|public
name|void
name|testEquals
parameter_list|()
block|{
name|MappedFieldType
name|ft1
init|=
name|createDefaultFieldType
argument_list|()
decl_stmt|;
name|MappedFieldType
name|ft2
init|=
name|createDefaultFieldType
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|ft1
argument_list|,
name|ft1
argument_list|)
expr_stmt|;
comment|// reflexive
name|assertEquals
argument_list|(
name|ft1
argument_list|,
name|ft2
argument_list|)
expr_stmt|;
comment|// symmetric
name|assertEquals
argument_list|(
name|ft2
argument_list|,
name|ft1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ft1
operator|.
name|hashCode
argument_list|()
argument_list|,
name|ft2
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numProperties
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|ft2
operator|=
name|createDefaultFieldType
argument_list|()
expr_stmt|;
name|modifyProperty
argument_list|(
name|ft2
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|ft1
argument_list|,
name|ft2
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|ft1
operator|.
name|hashCode
argument_list|()
argument_list|,
name|ft2
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testFreeze
specifier|public
name|void
name|testFreeze
parameter_list|()
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numProperties
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|MappedFieldType
name|fieldType
init|=
name|createDefaultFieldType
argument_list|()
decl_stmt|;
name|fieldType
operator|.
name|freeze
argument_list|()
expr_stmt|;
try|try
block|{
name|modifyProperty
argument_list|(
name|fieldType
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected already frozen exception for property "
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"already frozen"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

