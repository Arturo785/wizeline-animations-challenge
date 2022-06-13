package com.wizeline.academy.animations.ui.detail

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.wizeline.academy.animations.databinding.DetailFragmentBinding
import com.wizeline.academy.animations.utils.loadImage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DetailFragment : Fragment() {

    private var _binding: DetailFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailViewModel by viewModels()
    private val args: DetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DetailFragmentBinding.inflate(layoutInflater, container, false)

        setTransitionNames()
        sharedElementReturnTransition =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)

        binding.btnMoreDetails.setOnClickListener { goToMoreDetails() }
        binding.ivImageDetail.loadImage(args.imageId)
        return binding.root
    }

    private fun goToMoreDetails() {
        val extras = FragmentNavigatorExtras(
            binding.tvTitle to "title_transition",
            binding.ivImageDetail to "image_transition",
            binding.tvSubtitle to "description_transition"
        )

        val directions =
            DetailFragmentDirections.toMoreDetailsFragment(
                args.imageId,
                viewModel.contentIndex,
            )
        findNavController().navigate(directions, extras)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.title.observe(viewLifecycleOwner) { binding.tvTitle.text = it }
        viewModel.subtitle.observe(viewLifecycleOwner) { binding.tvSubtitle.text = it }

        postponeEnterTransition()
        startPostponedEnterTransition()
    }

    private fun setTransitionNames() {
        binding.tvTitle.transitionName = "title_transition"
        binding.ivImageDetail.transitionName = "image_transition"
        binding.tvSubtitle.transitionName = "description_transition"
    }
}