package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Created by Alvin
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    private Logger logger= LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    public ServerResponse addCategory(String categoryName, Integer parentId){
        if(parentId==null|| StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMsg("Fail to add category, wrong parameter!");
        }
        Category category=new Category();
        category.setParentId(parentId);
        category.setName(categoryName);
        category.setStatus(true);

        int rowCount=categoryMapper.insert(category);
        if(rowCount>0){
            return ServerResponse.createBySuccess("Add category successful!");
        }
        return ServerResponse.createByErrorMsg("Failed to add category!");
    }

    public ServerResponse updateCategoryName(Integer categoryId, String categoryName){
        if(categoryId==null||StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMsg("Failed to update category name, wrong parameter!");
        }
        Category category=new Category();
        category.setId(categoryId);
        category.setName(categoryName);

        int rowCount=categoryMapper.updateByPrimaryKeySelective(category);
        if(rowCount>0){
            return ServerResponse.createBySuccess("Update category name successful!");
        }
        return ServerResponse.createByErrorMsg("Failed to update category name!");
    }

    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId){
        List<Category> categoryList=categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if(CollectionUtils.isEmpty(categoryList)){
            logger.info("Cannot find children category of current category!");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId){
        Set<Category> categorySet= Sets.newHashSet();
        findChildCategory(categorySet,categoryId);

        List<Integer> categoryIdList= Lists.newArrayList();
        if(categoryId!=null){
            for(Category categoryItem:categorySet){
                categoryIdList.add(categoryItem.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryIdList);
    }

    private void findChildCategory(Set<Category> categorySet,Integer categoryId){
        Category category=categoryMapper.selectByPrimaryKey(categoryId);
        if(category!=null) {
            categorySet.add(category);
        }
        List<Category> categoryList=categoryMapper.selectCategoryChildrenByParentId(categoryId);
        for(Category categoryItem:categoryList){
            findChildCategory(categorySet,categoryItem.getId());
        }
    }


}
